const db = require('../config/db');
const grok = require('./grok.service');
const rag = require('./rag.service');

const { buildExtractPrompt } = require('../prompts/extract.prompt');
const { buildVerifyPrompt } = require('../prompts/verify.prompt');
const { buildRespondPrompt } = require('../prompts/respond.prompt');
const { buildTitlePrompt } = require('../prompts/title.prompt');

/**
 * Orchestrator AI Chat (Dr.Bug).
 *
 * Ghép toàn bộ luồng 1 lượt: EXTRACT → RETRIEVE → VERIFY → (backend quyết case) → RESPOND
 * → thực thi action → lưu chat_history + turn_count. Đồng thời quản lý vòng đời session
 * (tạo khi user gửi lượt đầu, sinh title khi kết thúc).
 *
 * Nguyên tắc quan trọng:
 *  - Session CHỈ tạo khi user gửi lượt đầu (không tạo khi mở UI / bấm "+").
 *  - turn_count chỉ tăng khi AI sinh text/action thật (select_priority KHÔNG tăng).
 *  - Title chỉ sinh khi session kết thúc (bất kỳ lượt nào).
 *  - Backend KHÔNG tin tuyệt đối LLM: kiểm lại leaf-node / phả hệ sau VERIFY.
 */

// Lượt cuối bắt buộc gọi function (không hỏi mở nữa).
const FINAL_TURN = 4;

// Ngưỡng confidence tối thiểu để chấp nhận user ĐỔI NHÁNH bằng text (phương án A).
// Dưới ngưỡng coi là nhiễu → giữ nguyên nhánh đang khóa, tránh bị "văng" scope vì 1 câu mơ hồ.
const PIVOT_MIN_CONFIDENCE = 70;

// Gợi ý Tầng 2 mặc định khi không có lịch sử (TH2 fallback). Khớp 3 gợi ý trong mockup.
const FALLBACK_T2_IDS = ['academic_project', 'academic_knowledge', 'rel_family'];

const OPENING_MESSAGE =
    'Xin chào! Mình là Dr.Bug.\n' +
    'Hôm nay bạn có đang gặp vấn đề nào không? ' +
    'Hãy trò chuyện với mình để mình hiểu thấu vấn đề của bạn nhé.';

// ===================== HELPERS =====================

async function getStudentId(userId) {
    const [rows] = await db.query(
        'SELECT student_id FROM students WHERE user_id = ?',
        [userId]
    );
    if (rows.length === 0) throw new Error('Không tìm thấy thông tin sinh viên');
    return rows[0].student_id;
}

async function loadProblem(id) {
    if (!id) return null;
    const [rows] = await db.query(
        'SELECT id, title, parent_id, tree_level, is_leaf_node FROM problems WHERE id = ?',
        [id]
    );
    if (rows.length === 0) return null;
    const p = rows[0];
    return { ...p, is_leaf_node: !!p.is_leaf_node };
}

/** Load các con TRỰC TIẾP của 1 node (để câu hỏi phân biệt trong RESPOND_VAGUE bám đúng
 *  nhánh có thật trong DB, thay vì để LLM tự bịa ra các hướng con). Trả về mảng title. */
async function loadChildren(parentId) {
    if (!parentId) return [];
    const [rows] = await db.query(
        'SELECT title FROM problems WHERE parent_id = ? ORDER BY id',
        [parentId]
    );
    return rows.map((r) => r.title);
}

/** Parse chat_history từ DB (MySQL JSON có thể trả string hoặc object tùy driver). */
function parseHistory(raw) {
    if (raw == null) return [];
    if (Array.isArray(raw)) return raw;
    try {
        return JSON.parse(raw);
    } catch {
        return [];
    }
}

/** Đổi chat_history nội bộ → mảng messages cho Grok (chỉ lấy text, bỏ metadata). */
function toGrokHistory(chatHistory, limit = 8) {
    return chatHistory
        .filter((m) => m.content)
        .slice(-limit)
        .map((m) => ({
            role: m.sender === 'user' ? 'user' : 'assistant',
            content: m.content
        }));
}

/** Đổi chat_history → text hội thoại nhiều dòng (cho TITLE). */
function historyToText(chatHistory) {
    return chatHistory
        .filter((m) => m.content)
        .map((m) => `${m.sender === 'user' ? 'User' : 'Dr.Bug'}: ${m.content}`)
        .join('\n');
}

function makeMessage(turn, sender, content, metadata = null) {
    return { turn, sender, content, timestamp: new Date().toISOString(), metadata };
}

/** Bỏ cặp ngoặc bao ngoài nếu LLM lỡ bọc cả câu trả lời (ví dụ trong prompt có bọc quote). */
function stripWrappingQuotes(text) {
    const t = (text || '').trim();
    if (t.length >= 2) {
        const f = t[0], l = t[t.length - 1];
        if ((f === '"' && l === '"') || (f === '“' && l === '”') || (f === "'" && l === "'")) {
            return t.slice(1, -1).trim();
        }
    }
    return t;
}

/** Lưu chat_history + các cột session về DB. */
async function persistSession(sessionId, { chatHistory, turnCount, status, resolvedProblemId, title, focusProblemId }) {
    const fields = ['chat_history = ?'];
    const values = [JSON.stringify(chatHistory)];

    if (turnCount !== undefined) { fields.push('turn_count = ?'); values.push(turnCount); }
    if (status !== undefined) { fields.push('status = ?'); values.push(status); }
    if (resolvedProblemId !== undefined) { fields.push('resolved_problem_id = ?'); values.push(resolvedProblemId); }
    if (title !== undefined) { fields.push('session_title = ?'); values.push(title); }
    if (focusProblemId !== undefined) { fields.push('focus_problem_id = ?'); values.push(focusProblemId); }

    values.push(sessionId);
    await db.query(`UPDATE ai_chat_sessions SET ${fields.join(', ')} WHERE session_id = ?`, values);
}

/** Tạo 1 session mới (status='active', turn 0, history rỗng) và trả về object session. */
async function createSession(studentId) {
    const [result] = await db.query(
        `INSERT INTO ai_chat_sessions (student_id, status, turn_count, chat_history)
         VALUES (?, 'active', 0, ?)`,
        [studentId, JSON.stringify([])]
    );
    return {
        session_id: result.insertId,
        student_id: studentId,
        status: 'active',
        turn_count: 0,
        chat_history: '[]',
        resolved_problem_id: null
    };
}

// ===================== QUEST =====================

/**
 * Lấy Top-3 quest ĐÃ DUYỆT gắn vào đúng 1 lá Tầng 3 (problemId).
 *  - Chỉ approved: JOIN quest_versions với version approved MỚI NHẤT (draft/pending/rejected bị loại).
 *  - Đúng lá: q.problem_id = ? (KHÔNG nới lên parent như catalog của Quest Builder).
 *  - Rank: base_priority DESC (cột ưu tiên của Quest Builder), reviewed_at DESC làm tie-break.
 *  - Fallback: lá chưa có quest approved → rows rỗng → placeholder=true (UI hiện "đang cập nhật").
 */
async function getTopQuests(problemId) {
    if (!problemId) return { quests: [], placeholder: true };
    const [rows] = await db.query(
        `SELECT q.quest_id, q.quest_title, q.quest_description,
                q.quest_level, q.base_priority, qv.version_id
         FROM quests q
         JOIN quest_versions qv ON qv.version_id = (
            SELECT qv2.version_id FROM quest_versions qv2
            WHERE qv2.quest_id = q.quest_id AND qv2.status = 'approved'
            ORDER BY qv2.version_number DESC LIMIT 1
         )
         WHERE q.is_active = 1 AND q.problem_id = ?
         ORDER BY q.base_priority DESC, qv.reviewed_at DESC
         LIMIT 3`,
        [problemId]
    );
    return { quests: rows, placeholder: rows.length === 0 };
}

// ===================== SUGGESTIONS (waterfall, không dùng LLM) =====================

/**
 * Chọn tối đa 3 nhóm lỗi Tầng 2 làm gợi ý mở đầu, theo waterfall:
 *   TH3 (ưu tiên cao): từ lịch sử session — resolved_problem_id gần nhất → truy parent (Tầng 2).
 *   TH1 (trung bình):  từ hệ thống Quest — TODO(quest), tạm bỏ qua.
 *   TH2 (fallback):    3 nhóm Tầng 2 mặc định.
 * Dedup theo id, giữ thứ tự ưu tiên.
 */
async function getSuggestions(studentId) {
    const orderedIds = [];

    // TH3: nhóm Tầng 2 nổi cộm từ lịch sử.
    const [historyRows] = await db.query(
        `SELECT p2.id AS t2_id, COUNT(*) AS freq
         FROM ai_chat_sessions s
         JOIN problems p3 ON s.resolved_problem_id = p3.id
         JOIN problems p2 ON p3.parent_id = p2.id
         WHERE s.student_id = ? AND s.resolved_problem_id IS NOT NULL
         GROUP BY p2.id
         ORDER BY freq DESC, MAX(s.updated_at) DESC
         LIMIT 3`,
        [studentId]
    );
    for (const r of historyRows) orderedIds.push(r.t2_id);

    // TH1: TODO(quest) — bổ sung từ quest-count khi có Quest Engine.

    // TH2: fallback mặc định, dedup.
    for (const id of FALLBACK_T2_IDS) {
        if (orderedIds.length >= 3) break;
        if (!orderedIds.includes(id)) orderedIds.push(id);
    }

    const finalIds = orderedIds.slice(0, 3);
    if (finalIds.length === 0) return [];

    // Lấy title, giữ đúng thứ tự ưu tiên.
    const placeholders = finalIds.map(() => '?').join(',');
    const [rows] = await db.query(
        `SELECT id, title FROM problems WHERE id IN (${placeholders})`,
        finalIds
    );
    const titleById = new Map(rows.map((r) => [r.id, r.title]));
    return finalIds
        .filter((id) => titleById.has(id))
        .map((id) => ({ id, title: titleById.get(id) }));
}

// ===================== PUBLIC: START SESSION =====================

/**
 * Mở UI chat. KHÔNG tạo row trong DB (đúng quy tắc: session chỉ tạo khi user gửi lượt đầu).
 * Trả lời chào + tối đa 3 gợi ý Tầng 2.
 */
async function startSession(userId) {
    const studentId = await getStudentId(userId);
    const suggestions = await getSuggestions(studentId);
    return { opening_message: OPENING_MESSAGE, suggestions };
}

// ===================== PUBLIC: HANDLE MESSAGE =====================

/**
 * Xử lý 1 lượt chat. Tạo session nếu chưa có.
 * @param {object} params
 * @param {number} params.userId
 * @param {number|null} params.sessionId - null nếu là lượt đầu (sẽ tạo session).
 * @param {string} params.text - nội dung user gửi.
 * @param {string|null} [params.pickedProblemId] - id Tầng 2 khi user bấm gợi ý mở đầu (Nhánh A).
 */
async function handleMessage({ userId, sessionId, text, pickedProblemId = null }) {
    if (!grok.isConfigured()) {
        const err = new Error('GROQ_API_KEY chưa được cấu hình');
        err.code = 'GROK_NOT_CONFIGURED';
        throw err;
    }
    if (!text || !text.trim()) throw new Error('Nội dung tin nhắn rỗng');

    const studentId = await getStudentId(userId);

    // --- Load hoặc tạo session ---
    let session;
    if (sessionId) {
        const [rows] = await db.query(
            'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND student_id = ?',
            [sessionId, studentId]
        );
        if (rows.length === 0) throw new Error('Không tìm thấy session');
        session = rows[0];
        // Session đã kết thúc (completed / pending_feedback) mà user vẫn gõ tiếp
        // → coi là mở chủ đề mới: tạo session mới, tin này thành lượt 1.
        // (Không nhồi vào session cũ vì turn đã ≥ FINAL_TURN, ngữ nghĩa lượt sẽ loạn.)
        if (session.status !== 'active') {
            session = await createSession(studentId);
        }
    } else {
        session = await createSession(studentId);
    }

    const chatHistory = parseHistory(session.chat_history);
    const currentTurn = session.turn_count + 1;

    // Append tin nhắn user (luôn lưu, kể cả khi sau đó là select_priority).
    chatHistory.push(makeMessage(currentTurn, 'user', text.trim()));

    // Persist tin nhắn user NGAY (trước pipeline RAG). Nếu bất kỳ bước downstream nào
    // (RETRIEVE/VERIFY/RESPOND/getTopQuests...) ném lỗi → tin nhắn user vẫn sống trong DB,
    // không bị mất khi user quay lại session. Chưa tăng turn_count (chờ produceAiTurn chốt).
    await persistSession(session.session_id, { chatHistory });

    // Nhánh scope hiện tại: ưu tiên focus mới (user vừa bấm Quick Reply), nếu không lấy từ session.
    let focusId = session.focus_problem_id || null;

    // --- Nhánh A: user bấm gợi ý → xử lý theo tầng của node được chọn ---
    if (pickedProblemId) {
        const picked = await loadProblem(pickedProblemId);
        if (picked) {
            if (picked.is_leaf_node) {
                // Bấm trúng lỗi Tầng 3 (hiếm — gợi ý mở đầu là Tầng 2) → chốt luôn.
                return produceAiTurn({
                    session, chatHistory, currentTurn, studentId,
                    decision: { kind: 'success', problem: picked },
                    userInput: text.trim(), grokHistory: toGrokHistory(chatHistory.slice(0, -1)),
                    focusId
                });
            }
            // Bấm nhóm Tầng 1/2 → khóa scope nhánh này cho các lượt sau, rồi hỏi sâu.
            focusId = picked.id;
            return produceAiTurn({
                session, chatHistory, currentTurn, studentId,
                decision: { kind: 'vague', groupTitle: picked.title },
                userInput: text.trim(), grokHistory: toGrokHistory(chatHistory.slice(0, -1)),
                focusId
            });
        }
    }

    // --- Lõi RAG: Extract → Retrieve → Verify ---
    // Phương án A (cho phép ĐỔI NHÁNH bằng text): LUÔN retrieve toàn cây (KHÔNG truyền focusId
    // vào pipeline) để VERIFY nhìn được mọi nhánh và tự phát hiện user có đang pivot hay không.
    // Việc "khóa nhánh" được xử lý SAU verify (giữ lock nếu không pivot, đổi nếu pivot mạnh) —
    // thay cho cách cũ hard-scope RETRIEVE khiến user bị nhốt trong nhánh đã chọn cho tới hết phiên.
    const grokHistory = toGrokHistory(chatHistory.slice(0, -1)); // history trước input hiện tại
    const verifyResult = await runRagPipeline(text.trim(), currentTurn, grokHistory, null);

    // Backend kiểm lại kết quả LLM (không tin tuyệt đối).
    const decision = await validateDecision(verifyResult);

    // --- select_priority: KHÔNG tăng turn, trả popup cho UI ---
    if (decision.kind === 'select_priority') {
        await persistSession(session.session_id, { chatHistory }); // chỉ lưu user msg
        return {
            session_id: session.session_id,
            turn: session.turn_count, // không tăng
            action: {
                action_type: 'select_priority',
                data: { candidates: decision.candidates }
            },
            ai_message: null,
            status: session.status
        };
    }

    // Quyết định nhánh scope cho lượt này:
    //  - Chưa khóa (focusId rỗng): auto-scope như cũ — khóa vào nhánh Tầng 2 nếu verify xác định được.
    //  - Đã khóa: mặc định GIỮ lock; chỉ đổi khi user pivot MẠNH sang nhánh khác (resolvePivotFocus).
    let effectiveFocus;
    if (!focusId) {
        effectiveFocus = decision.scopeId || null;
    } else {
        const pivot = await resolvePivotFocus(decision, verifyResult, focusId);
        effectiveFocus = pivot || focusId;
    }

    // --- Các case còn lại đều sinh AI text/action → turn tăng ---
    return produceAiTurn({
        session, chatHistory, currentTurn, studentId,
        decision, userInput: text.trim(), grokHistory, focusId: effectiveFocus
    });
}

/**
 * Chạy EXTRACT → RETRIEVE → VERIFY, trả về object verify đã parse.
 * @param {string|null} focusId - nếu có (user đã bấm Quick Reply nhánh [X]), RETRIEVE chỉ
 *   tìm trong nhánh đó (node + con cháu) — giữ đúng scope suốt các lượt sau.
 */
async function runRagPipeline(userInput, turnCount, grokHistory, focusId = null) {
    // [1] EXTRACT
    const ex = buildExtractPrompt(userInput);
    let queries = [];
    try {
        const extracted = await grok.chatJSON(ex.system, [], ex.userInput);
        queries = Array.isArray(extracted.queries) ? extracted.queries.filter(Boolean) : [];
    } catch (e) {
        console.error('[AICHAT] EXTRACT lỗi, fallback dùng nguyên input:', e.message);
    }
    if (queries.length === 0) queries = [userInput];

    // [2] RETRIEVE (local) — giới hạn theo nhánh nếu đang scope.
    let retrievedGroups = [];
    try {
        retrievedGroups = await rag.retrieve(queries, focusId);
    } catch (e) {
        console.error('[AICHAT] RETRIEVE lỗi, tiếp tục với danh sách rỗng:', e.message);
    }

    // [3] VERIFY — bọc try/catch như EXTRACT: nếu Groq trả JSON hỏng / timeout / rate-limit
    // thì KHÔNG để lỗi văng lên controller (gây 500 + mất tin nhắn user chưa kịp persist).
    // Hạ về vague để Dr.Bug vẫn hỏi tiếp bình thường; focusId đang khóa vẫn được giữ.
    const vf = buildVerifyPrompt({ userInput, retrievedGroups, turnCount, history: grokHistory });
    try {
        return await grok.chatJSON(vf.system, vf.history, vf.userInput);
    } catch (e) {
        console.error('[AICHAT] VERIFY lỗi, fallback vague:', e.message);
        return { decision: 'vague', mappings: [], resolved_problem_id: null, priority_candidates: [] };
    }
}

/**
 * Backend kiểm lại quyết định của VERIFY (chống LLM "ảo"):
 *  - success: phải có resolved_problem_id tồn tại VÀ is_leaf_node=true. Sai → hạ thành vague.
 *  - select_priority: phải có ≥2 candidates khác nhánh. Thiếu → hạ thành vague.
 * Với "vague", nếu xác định được nhánh Tầng 2 thì trả kèm scopeId để auto-scope các lượt sau.
 * @returns {{kind:'success'|'vague'|'offtopic'|'select_priority', problem?, candidates?, scopeId?, groupTitle?}}
 */
async function validateDecision(verifyResult) {
    const decision = verifyResult?.decision;

    if (decision === 'success') {
        const problem = await loadProblem(verifyResult.resolved_problem_id);
        if (problem && problem.is_leaf_node) {
            return { kind: 'success', problem };
        }
        // LLM nói success nhưng không phải lỗi Tầng 3 hợp lệ → hỏi sâu thêm.
        return await asVague(problem || null, verifyResult);
    }

    if (decision === 'select_priority') {
        // LLM hay chế sai id (title đúng nhưng id không tồn tại) → verify từng cái qua DB.
        // Cho phép mọi tầng có thật (Tầng 1 nhánh gốc rộng, Tầng 2 nhánh con, Tầng 3 lỗi
        // cụ thể). Dùng id+title thật trong DB nên popup khớp 100%.
        const raw = (verifyResult.priority_candidates || []).filter((c) => c && c.id);
        const verified = [];
        for (const c of raw) {
            const p = await loadProblem(c.id);
            if (p) verified.push(p);
        }
        if (verified.length >= 2) {
            const candidates = verified.map((p) => ({ id: p.id, title: p.title }));
            return { kind: 'select_priority', candidates };
        }
        // Chỉ còn 1 candidate hợp lệ → không cần hỏi chọn:
        //   Tầng 3 (lá) → chốt success luôn; Tầng 1/2 → hạ vague + scope vào nhánh để hỏi sâu.
        if (verified.length === 1) {
            const only = verified[0];
            if (only.is_leaf_node) return { kind: 'success', problem: only };
            return await asVague(only, verifyResult);
        }
        return await asVague(null, verifyResult);
    }

    if (decision === 'offtopic') return { kind: 'offtopic' };
    return await asVague(null, verifyResult);
}

/**
 * Dựng kết quả "vague" và xác định nhánh Tầng 2 để auto-scope (quy tắc 1A):
 *  - Match Tầng 2 → scope chính nó.
 *  - Match Tầng 3 (confidence thấp nên chưa chốt) → scope lên cha Tầng 2.
 *  - Match Tầng 1 → KHÔNG scope (còn quá rộng), chỉ lấy title làm ngữ cảnh.
 * scopeId chỉ set khi xác định được nhánh Tầng 2 cụ thể.
 */
async function asVague(loadedProblem, verifyResult) {
    let matched = loadedProblem;
    if (!matched) {
        const mappings = (verifyResult?.mappings || []).filter((m) => m && m.problem_id);
        if (mappings.length) {
            mappings.sort((a, b) => (b.confidence || 0) - (a.confidence || 0));
            matched = await loadProblem(mappings[0].problem_id);
        }
    }

    const result = { kind: 'vague' };
    if (!matched) return result;

    if (matched.tree_level === 2) {
        result.scopeId = matched.id;
        result.groupTitle = matched.title;
    } else if (matched.tree_level === 3 && matched.parent_id) {
        result.scopeId = matched.parent_id;
        const parent = await loadProblem(matched.parent_id);
        if (parent) result.groupTitle = parent.title;
    } else if (matched.tree_level === 1) {
        result.groupTitle = matched.title; // Tầng 1: chỉ ngữ cảnh, không scope.
    }
    return result;
}

/**
 * Node `nodeId` có nằm trong nhánh gốc `ancestorId` không (chính nó hoặc con cháu)?
 * Leo parent_id từ node lên gốc; gặp ancestorId → true. Dùng để phân biệt "vẫn trong nhánh
 * đang khóa" với "đã sang nhánh khác" (pivot). Giới hạn 10 vòng phòng dữ liệu vòng lặp.
 */
async function isDescendantOrSelf(nodeId, ancestorId) {
    if (!nodeId || !ancestorId) return false;
    let cur = nodeId;
    for (let i = 0; i < 10 && cur; i++) {
        if (cur === ancestorId) return true;
        const p = await loadProblem(cur);
        cur = p ? p.parent_id : null;
    }
    return false;
}

/**
 * Phương án A — khi ĐANG khóa nhánh (focusId) và user nhắn text: có ĐỔI nhánh không?
 * Trả về id nhánh MỚI (Tầng 2) nếu user pivot MẠNH sang nhánh khác; null nếu giữ nguyên khóa.
 *
 * Điều kiện pivot (phải đủ cả 3 để tránh văng scope vì 1 câu mơ hồ):
 *  1. Xác định được node đích của lượt (success → lá; vague → scopeId; hoặc mapping mạnh nhất).
 *  2. success được coi là đủ mạnh (model đã cam kết 1 lá); còn lại confidence ≥ ngưỡng.
 *  3. Node đích KHÔNG thuộc nhánh đang khóa (thật sự khác nhánh).
 */
async function resolvePivotFocus(decision, verifyResult, focusId) {
    const mappings = (verifyResult?.mappings || []).filter((m) => m && m.problem_id);
    mappings.sort((a, b) => (b.confidence || 0) - (a.confidence || 0));
    const top = mappings[0];

    let targetId = decision.kind === 'success'
        ? (decision.problem ? decision.problem.id : null)
        : (decision.scopeId || null);
    if (!targetId && top) targetId = top.problem_id;
    if (!targetId) return null;

    // success = đã chốt lá → đủ mạnh; các case khác cần confidence vượt ngưỡng.
    if (decision.kind !== 'success' && (top ? (top.confidence || 0) : 0) < PIVOT_MIN_CONFIDENCE) {
        return null;
    }

    // Vẫn trong nhánh đang khóa → không phải pivot.
    if (await isDescendantOrSelf(targetId, focusId)) return null;

    // Nhánh mới để khóa: ưu tiên Tầng 2 (scopeId có sẵn cho vague; success/khác → leo lên cha T2).
    let newFocus = decision.scopeId || null;
    if (!newFocus) {
        const node = await loadProblem(targetId);
        if (node) {
            if (node.tree_level === 2) newFocus = node.id;
            else if (node.tree_level === 3 && node.parent_id) newFocus = node.parent_id;
            // Tầng 1: quá rộng, không khóa cụ thể → newFocus giữ null.
        }
    }
    if (!newFocus || newFocus === focusId) return null;
    if (await isDescendantOrSelf(newFocus, focusId)) return null; // an toàn: cùng nhánh thì bỏ
    return newFocus;
}

/**
 * Sinh AI text/action theo (decision + currentTurn), lưu DB, tăng turn.
 * Xử lý cả luật lượt cuối (FINAL_TURN).
 * @param {string|null} [focusId] - nhánh scope hiện tại (từ Quick Reply); persist để giữ qua các lượt.
 */
async function produceAiTurn({ session, chatHistory, currentTurn, studentId, decision, userInput, grokHistory, focusId = null }) {
    const isFinalTurn = currentTurn >= FINAL_TURN;

    // Guard (phương án A): lượt cuối, ĐANG khóa nhánh (focusId) mà VERIFY buông 'offtopic'
    // → KHÔNG đẩy sang Git Journal. Khi đã cam kết 1 nhánh cụ thể, user vẫn đang trong chủ đề đó
    // (offtopic thường do câu quá lệch so với title trong nhánh, không phải đổi chuyện) → ép về
    // 'vague' để rơi vào popup 4.2 (quest thư giãn / cộng đồng), đúng ngữ nghĩa "trong nhánh nhưng
    // chưa chốt được lá". Không khóa focus thì giữ nguyên hành vi cũ (offtopic → Git Journal).
    if (isFinalTurn && focusId && decision.kind === 'offtopic') {
        decision = { ...decision, kind: 'vague' };
    }

    // --- Trường hợp 4.2: lượt cuối mà VẪN mơ hồ (chưa chốt được lỗi Tầng 3) ---
    // KHÔNG ép hướng. Đưa 2 lựa chọn để user tự quyết (quest thư giãn / lên cộng đồng).
    // Mirror select_priority: KHÔNG tăng turn, không sinh bubble AI, chờ user chọn ở /route.
    if (isFinalTurn && decision.kind === 'vague') {
        const focusChanged = (focusId || null) !== (session.focus_problem_id || null);
        await persistSession(session.session_id, {
            chatHistory, // chỉ có tin nhắn user (bubble AI sinh sau khi user chọn route)
            ...(focusChanged ? { focusProblemId: focusId || null } : {})
        });
        return {
            session_id: session.session_id,
            turn: session.turn_count, // không tăng — chờ user chọn
            ai_message: null,
            action: {
                action_type: 'select_route',
                data: {
                    options: [
                        { key: 'quest', label: 'Gợi ý cho mình vài quest thư giãn' },
                        { key: 'community', label: 'Chia sẻ lên Cộng đồng để nhận lời khuyên' }
                    ]
                }
            },
            status: session.status
        };
    }

    let variant;
    let action = null;
    let endStatus = null;        // status khi session kết thúc
    let resolvedProblemId;       // chỉ set khi success
    let problemTitle = decision.problem ? decision.problem.title : undefined;
    let groupTitle = decision.groupTitle;

    if (decision.kind === 'success') {
        // Chốt được lỗi Tầng 3 → trigger_quest (mọi lượt, kể cả lượt cuối 4.1).
        variant = 'success';
        resolvedProblemId = decision.problem.id;
        const top = await getTopQuests(decision.problem.id);
        action = {
            action_type: 'show_quests',
            data: { problem_id: decision.problem.id, quests: top.quests, placeholder: top.placeholder }
        };
        endStatus = 'pending_feedback'; // chờ user làm quest rồi đánh giá
    } else if (isFinalTurn) {
        // Lượt cuối, off-topic vui vẻ (4.3) → Git Journal.
        // (Nhánh 4.2 "vague" đã được xử lý bằng popup select_route ở đầu hàm — không tới đây.)
        variant = 'turn4_journal';
        action = {
            action_type: 'redirect_feature',
            data: { target_screen: 'git_journal' }
        };
        endStatus = 'completed';
    } else {
        // Lượt thường (<4), chưa chốt: hỏi sâu hoặc chuyển hướng nhẹ.
        variant = decision.kind === 'offtopic' ? 'offtopic' : 'vague';
    }

    // Với vague: nếu đã khóa scope vào 1 nhánh (focusId), lấy các hướng con CÓ THẬT để
    // câu hỏi phân biệt bám đúng nhánh, tránh LLM tự bịa hướng con không tồn tại.
    let childTitles;
    if (variant === 'vague' && focusId) {
        const children = await loadChildren(focusId);
        if (children.length) childTitles = children;
    }

    // Sinh câu trả lời tiếng Việt.
    const rp = buildRespondPrompt(variant, { problemTitle, groupTitle, userInput, childTitles, history: grokHistory });
    let aiText;
    try {
        aiText = await grok.chatText(rp.system, rp.history, rp.userInput, { temperature: 0.7 });
    } catch (e) {
        console.error('[AICHAT] RESPOND lỗi:', e.message);
        aiText = 'Mình đang nghe bạn đây. Bạn chia sẻ thêm một chút để mình hiểu rõ hơn nhé?';
    }
    aiText = stripWrappingQuotes(aiText);

    chatHistory.push(makeMessage(currentTurn, 'ai', aiText, action));

    const newStatus = endStatus || session.status;

    // Nếu session kết thúc → sinh title.
    let title;
    if (endStatus) {
        title = await generateTitle(chatHistory);
    }

    // focus_problem_id: persist nếu khác giá trị hiện có trong session (mới khóa scope hoặc đổi).
    const focusChanged = (focusId || null) !== (session.focus_problem_id || null);

    await persistSession(session.session_id, {
        chatHistory,
        turnCount: currentTurn,
        status: newStatus,
        ...(resolvedProblemId !== undefined ? { resolvedProblemId } : {}),
        ...(title !== undefined ? { title } : {}),
        ...(focusChanged ? { focusProblemId: focusId || null } : {})
    });

    return {
        session_id: session.session_id,
        turn: currentTurn,
        ai_message: { content: aiText.trim(), metadata: action },
        action,
        status: newStatus,
        ...(title !== undefined ? { title } : {})
    };
}

/** Sinh tiêu đề từ toàn bộ hội thoại (gọi khi session kết thúc). */
async function generateTitle(chatHistory) {
    try {
        const tp = buildTitlePrompt(historyToText(chatHistory));
        const raw = await grok.chatText(tp.system, [], tp.userInput, { temperature: 0.3, maxTokens: 32 });
        return raw.trim().replace(/^["']|["']$/g, '').slice(0, 255);
    } catch (e) {
        console.error('[AICHAT] TITLE lỗi:', e.message);
        return 'Cuộc trò chuyện với Dr.Bug';
    }
}

// ===================== PUBLIC: PICK PRIORITY =====================

/**
 * User chọn 1 vấn đề ưu tiên sau popup select_priority. KHÔNG phải lượt mới — tiếp tục
 * lượt đang chờ với problem đã chọn. Tùy Tầng của vấn đề → success (Tầng 3) hoặc vague.
 */
async function pickPriority({ userId, sessionId, problemId }) {
    const studentId = await getStudentId(userId);
    const [rows] = await db.query(
        'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND student_id = ?',
        [sessionId, studentId]
    );
    if (rows.length === 0) throw new Error('Không tìm thấy session');
    const session = rows[0];

    const problem = await loadProblem(problemId);
    if (!problem) throw new Error('Vấn đề không hợp lệ');

    const chatHistory = parseHistory(session.chat_history);
    const currentTurn = session.turn_count + 1; // lượt đang chờ (user msg đã lưu trước đó)

    // Chọn lỗi Tầng 3 → chốt luôn. Chọn nhóm Tầng 1/2 → khóa scope nhánh đó rồi hỏi sâu.
    const decision = problem.is_leaf_node
        ? { kind: 'success', problem }
        : { kind: 'vague', groupTitle: problem.title };
    const focusId = problem.is_leaf_node
        ? (session.focus_problem_id || null)
        : problem.id;

    // userInput: lấy tin nhắn user gần nhất để RESPOND bám ngữ cảnh.
    const lastUser = [...chatHistory].reverse().find((m) => m.sender === 'user');
    const userInput = lastUser ? lastUser.content : '';

    return produceAiTurn({
        session, chatHistory, currentTurn, studentId,
        decision, userInput, grokHistory: toGrokHistory(chatHistory), focusId
    });
}

// ===================== PUBLIC: PICK ROUTE (lượt cuối 4.2) =====================

/**
 * User chọn hướng ở popup select_route (lượt cuối vẫn mơ hồ):
 *  - 'quest'     → quest thư giãn tức thời (placeholder), status='pending_feedback'.
 *  - 'community' → chuyển sang Cộng đồng, status='completed'.
 * KHÔNG phải lượt mới: dùng lại lượt đang chờ (turn_count + 1), user msg đã lưu từ trước.
 */
async function pickRoute({ userId, sessionId, routeKey }) {
    const studentId = await getStudentId(userId);
    const [rows] = await db.query(
        'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND student_id = ?',
        [sessionId, studentId]
    );
    if (rows.length === 0) throw new Error('Không tìm thấy session');
    const session = rows[0];

    const chatHistory = parseHistory(session.chat_history);
    const currentTurn = session.turn_count + 1;
    const grokHistory = toGrokHistory(chatHistory);

    let variant, action, endStatus;
    if (routeKey === 'quest') {
        // 4.2a — quest thư giãn tức thời. Quest Engine chưa build → placeholder (2A).
        // TODO(quest): thay bằng nhóm quest thư giãn thật khi Quest Engine sẵn sàng.
        variant = 'turn4_quest';
        action = {
            action_type: 'show_quests',
            data: { problem_id: null, quests: [], placeholder: true, fallback: true }
        };
        endStatus = 'pending_feedback';
    } else {
        // 4.2b — lên Cộng đồng.
        variant = 'turn4_community';
        action = { action_type: 'redirect_feature', data: { target_screen: 'community' } };
        endStatus = 'completed';
    }

    const lastUser = [...chatHistory].reverse().find((m) => m.sender === 'user');
    const userInput = lastUser ? lastUser.content : '';

    const rp = buildRespondPrompt(variant, { userInput, history: grokHistory });
    let aiText;
    try {
        aiText = await grok.chatText(rp.system, rp.history, rp.userInput, { temperature: 0.7 });
    } catch (e) {
        console.error('[AICHAT] RESPOND (route) lỗi:', e.message);
        aiText = 'Mình đã ghi nhận lựa chọn của bạn.';
    }
    aiText = stripWrappingQuotes(aiText);

    chatHistory.push(makeMessage(currentTurn, 'ai', aiText, action));
    const title = await generateTitle(chatHistory);

    await persistSession(session.session_id, {
        chatHistory, turnCount: currentTurn, status: endStatus, title
    });

    return {
        session_id: session.session_id,
        turn: currentTurn,
        ai_message: { content: aiText.trim(), metadata: action },
        action,
        status: endStatus,
        title
    };
}

// ===================== PUBLIC: SESSION LIST / DETAIL / END =====================

/** Danh sách session của user (cho màn 2). session_title NULL khi chưa kết thúc. */
async function getSessions(userId) {
    const studentId = await getStudentId(userId);
    const [rows] = await db.query(
        `SELECT session_id, session_title, status, turn_count, resolved_problem_id, created_at, updated_at
         FROM ai_chat_sessions
         WHERE student_id = ?
         ORDER BY updated_at DESC`,
        [studentId]
    );
    return rows;
}

/** Load 1 session: trả nguyên chat_history để UI render lại. */
async function getSession(userId, sessionId) {
    const studentId = await getStudentId(userId);
    const [rows] = await db.query(
        'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND student_id = ?',
        [sessionId, studentId]
    );
    if (rows.length === 0) throw new Error('Không tìm thấy session');
    const s = rows[0];
    return {
        session_id: s.session_id,
        session_title: s.session_title,
        status: s.status,
        turn_count: s.turn_count,
        resolved_problem_id: s.resolved_problem_id,
        chat_history: parseHistory(s.chat_history)
    };
}

/** User chủ động kết thúc session → sinh title, status='completed'. */
async function endSession(userId, sessionId) {
    const studentId = await getStudentId(userId);
    const [rows] = await db.query(
        'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND student_id = ?',
        [sessionId, studentId]
    );
    if (rows.length === 0) throw new Error('Không tìm thấy session');
    const session = rows[0];

    const chatHistory = parseHistory(session.chat_history);
    const title = session.session_title || await generateTitle(chatHistory);
    const newStatus = session.status === 'pending_feedback' ? session.status : 'completed';

    await persistSession(sessionId, { chatHistory, status: newStatus, title });
    return { session_id: sessionId, session_title: title, status: newStatus };
}

module.exports = {
    startSession,
    handleMessage,
    pickPriority,
    pickRoute,
    getSessions,
    getSession,
    endSession
};
