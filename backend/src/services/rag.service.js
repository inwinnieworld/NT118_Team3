const db = require('../config/db');

/**
 * RAG service — tầng RETRIEVE của luồng chat (vai "thủ thư": chỉ mang về ứng viên,
 * KHÔNG quyết định đúng/sai — việc đó để LLM Verify).
 *
 * Hybrid Search = Semantic (embeddings local) + Keyword (MySQL LIKE):
 *   - Semantic: biến câu thành vector rồi so cosine với vector của từng node `problems`.
 *     Bắt được câu khác chữ nhưng cùng nghĩa ("buồn vì điểm kém" ≈ "điểm số thấp").
 *   - Keyword: LIKE trên title để vớt các trùng từ khóa mà semantic có thể bỏ sót.
 *
 * Embeddings chạy local bằng @xenova/transformers (model đa ngữ). Tải model 1 lần lúc
 * server start rồi cache RAM; các lần sau chạy offline, không cần internet, không cần key.
 *
 * Cache RAM: vector của toàn bộ `problems` được tính sẵn lúc init() để mỗi request chỉ
 * phải embed query (rẻ) rồi so cosine với cache (không gọi lại model cho cả cây).
 */

// Model đa ngữ nhẹ, hỗ trợ tiếng Việt tốt, output 384 chiều.
const EMBED_MODEL = 'Xenova/paraphrase-multilingual-MiniLM-L12-v2';

// Số ứng viên trả về cho mỗi query (k trong k-candidates).
const TOP_K = 3;

// Trạng thái module-level (cache RAM).
let extractor = null;          // pipeline embedding đã tải
let problemVectors = [];       // [{ id, title, tree_level, is_leaf_node, parent_id, vector:Float32Array }]
let initialized = false;
let initPromise = null;

/**
 * Tải model + tính embedding cho toàn bộ `problems`. Gọi 1 lần lúc server start.
 * Idempotent: gọi nhiều lần chỉ chạy init thật 1 lần.
 */
async function init() {
    if (initialized) return;
    if (initPromise) return initPromise;

    initPromise = (async () => {
        console.log('[RAG] Đang tải model embedding (lần đầu có thể tải ~100-400MB)...');
        // import động vì @xenova/transformers là ESM-only.
        const { pipeline } = await import('@xenova/transformers');
        extractor = await pipeline('feature-extraction', EMBED_MODEL);
        console.log('[RAG] Model embedding sẵn sàng. Đang tính vector cho cây problems...');

        const [rows] = await db.query(
            'SELECT id, title, tree_level, is_leaf_node, parent_id FROM problems'
        );

        problemVectors = [];
        for (const row of rows) {
            const vector = await embed(row.title);
            problemVectors.push({
                id: row.id,
                title: row.title,
                tree_level: row.tree_level,
                is_leaf_node: !!row.is_leaf_node,
                parent_id: row.parent_id,
                vector
            });
        }

        initialized = true;
        console.log(`[RAG] Đã tính vector cho ${problemVectors.length} node problems. RAG sẵn sàng.`);
    })();

    return initPromise;
}

/** Biến 1 câu thành vector (mean-pooling + normalize) → Float32Array. */
async function embed(text) {
    const output = await extractor(text, { pooling: 'mean', normalize: true });
    return output.data; // Float32Array, đã normalize (||v|| = 1)
}

/** Cosine similarity. Vector đã normalize nên chỉ cần dot product. */
function cosine(a, b) {
    let dot = 0;
    for (let i = 0; i < a.length; i++) dot += a[i] * b[i];
    return dot;
}

/**
 * Tập id được phép tìm khi scope theo nhánh (focus node + toàn bộ con cháu).
 * Dùng cho Nhánh A: user bấm Quick Reply nhóm [X] → chỉ tìm lỗi thuộc nhánh [X].
 * @param {string} focusId - id node Tầng 1/2 đã chọn.
 * @returns {Set<string>|null} null nếu focusId rỗng/không tồn tại → tìm toàn cây.
 */
function descendantIds(focusId) {
    if (!focusId) return null;
    if (!problemVectors.some((p) => p.id === focusId)) return null;

    // children[parent_id] = [id, ...] để duyệt xuống nhanh.
    const children = new Map();
    for (const p of problemVectors) {
        if (!p.parent_id) continue;
        if (!children.has(p.parent_id)) children.set(p.parent_id, []);
        children.get(p.parent_id).push(p.id);
    }

    const allowed = new Set([focusId]);
    const stack = [focusId];
    while (stack.length) {
        const cur = stack.pop();
        for (const child of children.get(cur) || []) {
            if (!allowed.has(child)) {
                allowed.add(child);
                stack.push(child);
            }
        }
    }
    return allowed;
}

/**
 * Retrieve hybrid cho 1 mảng query.
 * @param {string[]} queries - các cụm từ đã được EXTRACT tách & lọc nhiễu.
 * @param {string|null} [focusId] - nếu có, chỉ tìm trong nhánh này (node + con cháu).
 * @returns {Promise<Array<{query:string, candidates:Array}>>}
 *   candidates: [{ id, title, tree_level, is_leaf_node, parent_id, score }] (đã dedup, top-k)
 */
async function retrieve(queries, focusId = null) {
    if (!initialized) await init();
    if (!Array.isArray(queries) || queries.length === 0) return [];

    const allowed = descendantIds(focusId);
    const results = [];
    for (const query of queries) {
        const candidates = await retrieveOne(query, allowed);
        results.push({ query, candidates });
    }
    return results;
}

/**
 * Retrieve cho 1 query: gộp semantic + keyword, dedup theo id, lấy top-k theo score.
 * @param {string} query
 * @param {Set<string>|null} allowed - nếu khác null, chỉ xét node có id trong tập này.
 */
async function retrieveOne(query, allowed = null) {
    // --- Semantic: cosine giữa query và cây (đã lọc theo nhánh nếu có allowed) ---
    const pool = allowed ? problemVectors.filter((p) => allowed.has(p.id)) : problemVectors;
    const qVec = await embed(query);
    const scored = pool.map((p) => ({
        id: p.id,
        title: p.title,
        tree_level: p.tree_level,
        is_leaf_node: p.is_leaf_node,
        parent_id: p.parent_id,
        score: cosine(qVec, p.vector)
    }));

    // --- Keyword: LIKE trên title. Boost điểm cho node khớp từ khóa ---
    const keywordIds = await keywordSearch(query);

    // Map id → candidate để gộp.
    const byId = new Map();
    for (const s of scored) byId.set(s.id, s);

    // Boost: node nào khớp keyword cộng thêm 0.15 vào score semantic (đẩy lên top).
    // Chỉ boost node nằm trong tập allowed (nếu đang scope theo nhánh).
    for (const id of keywordIds) {
        if (allowed && !allowed.has(id)) continue;
        const c = byId.get(id);
        if (c) c.score += 0.15;
    }

    // Sắp giảm dần theo score, lấy top-k.
    const sorted = [...byId.values()].sort((a, b) => b.score - a.score);
    return sorted.slice(0, TOP_K).map((c) => ({
        id: c.id,
        title: c.title,
        tree_level: c.tree_level,
        is_leaf_node: c.is_leaf_node,
        parent_id: c.parent_id,
        score: Number(c.score.toFixed(4))
    }));
}

/** Keyword search đơn giản bằng LIKE — trả về danh sách id khớp. */
async function keywordSearch(query) {
    const like = `%${query.trim()}%`;
    const [rows] = await db.query(
        'SELECT id FROM problems WHERE title LIKE ? LIMIT 10',
        [like]
    );
    return rows.map((r) => r.id);
}

function isReady() {
    return initialized;
}

module.exports = { init, retrieve, isReady, TOP_K };
