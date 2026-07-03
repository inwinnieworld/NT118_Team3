const db = require('../config/db');
const { ok, fail } = require('../utils/response');

const ENGINE_CATALOG = [
    { engine_type: 'flow', engine_subtype: 'sequential', symbol: '->' },
    { engine_type: 'flow', engine_subtype: 'parallel', symbol: '||' },
    { engine_type: 'flow', engine_subtype: 'composite', symbol: 'rectangle' },
    { engine_type: 'flow', engine_subtype: 'quest', symbol: 'Q' },
    { engine_type: 'media', engine_subtype: 'image' },
    { engine_type: 'media', engine_subtype: 'video' },
    { engine_type: 'media', engine_subtype: 'audio' },
    { engine_type: 'input', engine_subtype: 'gesture' },
    { engine_type: 'input', engine_subtype: 'sensor' },
    { engine_type: 'input', engine_subtype: 'voice' },
    { engine_type: 'input', engine_subtype: 'text_input' },
    { engine_type: 'output', engine_subtype: 'text' },
    { engine_type: 'output', engine_subtype: 'timer' }
];

const VALID_ENGINE_TYPES = new Set(ENGINE_CATALOG.map((engine) => engine.engine_type));
const VALID_ENGINE_SUBTYPES = new Set(ENGINE_CATALOG.map((engine) => engine.engine_subtype));

function normalizeJson(value, fallback = {}) {
    if (value === undefined || value === null) return JSON.stringify(fallback);
    return JSON.stringify(value);
}

function parseJsonColumn(value, fallback) {
    if (value === null || value === undefined) return fallback;
    if (typeof value === 'object') return value;
    try {
        return JSON.parse(value);
    } catch (_) {
        return fallback;
    }
}

function normalizeTags(value) {
    const raw = Array.isArray(value) ? value : String(value || '').split(',');
    return [...new Set(raw
        .map((tag) => String(tag).trim().toLowerCase())
        .filter(Boolean))]
        .slice(0, 20);
}

function clampNumber(value, minimum, maximum, fallback) {
    const number = Number(value);
    if (!Number.isFinite(number)) return fallback;
    return Math.max(minimum, Math.min(maximum, Math.round(number)));
}

function apiEngineType(subtype) {
    if (['sequential', 'parallel', 'composite', 'quest'].includes(subtype)) return 'flow';
    if (['image', 'video', 'audio'].includes(subtype)) return 'media';
    if (['gesture', 'sensor', 'voice', 'text_input'].includes(subtype)) return 'input';
    return 'output';
}

function basicCompletion(node) {
    const config = node.config || {};
    const attached = config.attached_engine || null;
    if (attached) {
        if (attached.engine_subtype === 'timer') return { type: 'timer', config: attached };
        if (attached.engine_subtype === 'text_input') return { type: 'input_submitted', config: attached };
        if (attached.engine_subtype === 'voice') return { type: 'voice_finished', config: attached };
        if (attached.engine_subtype === 'sensor') return { type: 'sensor_finished', config: attached };
        if (attached.engine_subtype === 'gesture') {
            const gesture = ['tap', 'spam_tap', 'swipe', 'hold'].includes(attached.gesture_type)
                ? attached.gesture_type : 'manual';
            return { type: gesture, config: attached };
        }
    }
    if (node.engine_subtype === 'timer') return { type: 'timer', config };
    if (node.engine_subtype === 'text_input') return { type: 'input_submitted', config };
    if (node.engine_subtype === 'voice') return { type: 'voice_finished', config };
    if (node.engine_subtype === 'sensor') return { type: 'sensor_finished', config };
    if (node.engine_subtype === 'gesture') {
        const gesture = ['tap', 'spam_tap', 'swipe', 'hold'].includes(config.gesture_type)
            ? config.gesture_type : 'manual';
        return { type: gesture, config };
    }
    if (['image', 'video', 'audio'].includes(node.engine_subtype)) {
        return { type: 'media_finished', config };
    }
    return { type: 'auto', config: {} };
}

function validateFlow(flow) {
    if (!flow || !Array.isArray(flow.nodes)) {
        return 'Flow phải có nodes dạng mảng';
    }

    const seen = new Set();
    for (const node of flow.nodes) {
        if (!node.client_node_id) return 'Mỗi node phải có client_node_id';
        if (seen.has(node.client_node_id)) return `Node bị trùng id: ${node.client_node_id}`;
        if (!VALID_ENGINE_TYPES.has(node.engine_type)) return `engine_type không hợp lệ: ${node.engine_type}`;
        if (!VALID_ENGINE_SUBTYPES.has(node.engine_subtype)) return `engine_subtype không hợp lệ: ${node.engine_subtype}`;
        seen.add(node.client_node_id);
    }

    for (const edge of flow.edges || []) {
        if (!edge.source_client_node_id || !edge.target_client_node_id) {
            return 'Mỗi edge phải có source_client_node_id và target_client_node_id';
        }
        if (!seen.has(edge.source_client_node_id)) return `Edge trỏ tới source không tồn tại: ${edge.source_client_node_id}`;
        if (!seen.has(edge.target_client_node_id)) return `Edge trỏ tới target không tồn tại: ${edge.target_client_node_id}`;
    }

    return null;
}

async function getStaffId(userId) {
    const [[staff]] = await db.query('SELECT staff_id FROM staff WHERE user_id = ?', [userId]);
    return staff ? staff.staff_id : null;
}

async function getAdminId(userId) {
    const [[admin]] = await db.query('SELECT admin_id FROM admins WHERE user_id = ?', [userId]);
    return admin ? admin.admin_id : null;
}

async function canReadQuest(userId, questId) {
    if (await getAdminId(userId)) return true;
    const staffId = await getStaffId(userId);
    if (!staffId) return false;
    const [[quest]] = await db.query(
        'SELECT quest_id FROM quests WHERE quest_id = ? AND created_by_staff_id = ?',
        [questId, staffId]
    );
    return Boolean(quest);
}

async function getStudentId(userId) {
    const [[student]] = await db.query('SELECT student_id FROM students WHERE user_id = ?', [userId]);
    return student ? student.student_id : null;
}

async function loadQuestVersion(versionId) {
    const [[version]] = await db.query(
        `SELECT qv.version_id, qv.quest_id, qv.version_number, qv.status AS version_status,
                qv.canvas_config, q.quest_title, q.quest_description, q.quest_level,
                q.problem_id, qv.status AS approval_status,
                problem.title AS problem_title,
                CONCAT_WS(' > ', root_problem.title, parent_problem.title, problem.title) AS problem_path
         FROM quest_versions qv
         JOIN quests q ON qv.quest_id = q.quest_id
         LEFT JOIN problems problem ON problem.id = q.problem_id
         LEFT JOIN problems parent_problem ON parent_problem.id = problem.parent_id
         LEFT JOIN problems root_problem ON root_problem.id = parent_problem.parent_id
         WHERE qv.version_id = ?`,
        [versionId]
    );

    if (!version) return null;

    const [basicConfigs] = await db.query(
        `SELECT basic.basic_config_id, basic.client_config_id, parent.client_config_id AS parent_client_config_id,
                engine.engine_subtype, basic.display_name, basic.position_x, basic.position_y,
                basic.width, basic.height, basic.z_index, basic.config
         FROM basic_engine_configs basic
         JOIN engines engine ON engine.engine_id = basic.engine_id
         LEFT JOIN flow_engine_configs parent ON parent.flow_config_id = basic.parent_flow_config_id
         WHERE basic.version_id = ?
         ORDER BY basic.z_index ASC, basic.basic_config_id ASC`,
        [versionId]
    );

    const [flowConfigs] = await db.query(
        `SELECT flow.flow_config_id, flow.client_config_id, parent.client_config_id AS parent_client_config_id,
                COALESCE(terminal_basic.client_config_id, terminal_flow.client_config_id)
                    AS terminal_client_config_id,
                engine.engine_subtype, engine.engine_name, flow.display_name,
                flow.position_x, flow.position_y, flow.width, flow.height, flow.z_index,
                flow.completion_condition, flow.config
         FROM flow_engine_configs flow
         JOIN engines engine ON engine.engine_id = flow.engine_id
         LEFT JOIN flow_engine_configs parent ON parent.flow_config_id = flow.parent_flow_config_id
         LEFT JOIN basic_engine_configs terminal_basic
                ON terminal_basic.basic_config_id = flow.terminal_basic_config_id
         LEFT JOIN flow_engine_configs terminal_flow
                ON terminal_flow.flow_config_id = flow.terminal_flow_config_id
         WHERE flow.version_id = ? AND engine.engine_subtype <> 'sequential'
           AND flow.source_basic_config_id IS NULL
           AND flow.source_flow_config_id IS NULL
           AND flow.destination_basic_config_id IS NULL
           AND flow.destination_flow_config_id IS NULL
         ORDER BY flow.z_index ASC, flow.flow_config_id ASC`,
        [versionId]
    );

    const [connectorConfigs] = await db.query(
        `SELECT flow.client_config_id, engine.engine_subtype,
                COALESCE(source_basic.client_config_id, source_flow.client_config_id) AS source_client_config_id,
                COALESCE(destination_basic.client_config_id, destination_flow.client_config_id) AS destination_client_config_id,
                flow.sequence_order, flow.transition_type, flow.transition_config, flow.completion_condition
         FROM flow_engine_configs flow
         JOIN engines engine ON engine.engine_id = flow.engine_id AND engine.engine_subtype IN ('sequential', 'parallel')
         LEFT JOIN basic_engine_configs source_basic ON source_basic.basic_config_id = flow.source_basic_config_id
         LEFT JOIN flow_engine_configs source_flow ON source_flow.flow_config_id = flow.source_flow_config_id
         LEFT JOIN basic_engine_configs destination_basic ON destination_basic.basic_config_id = flow.destination_basic_config_id
         LEFT JOIN flow_engine_configs destination_flow ON destination_flow.flow_config_id = flow.destination_flow_config_id
         WHERE flow.version_id = ?
           AND (flow.source_basic_config_id IS NOT NULL OR flow.source_flow_config_id IS NOT NULL)
           AND (flow.destination_basic_config_id IS NOT NULL OR flow.destination_flow_config_id IS NOT NULL)
         ORDER BY flow.sequence_order ASC, flow.flow_config_id ASC`,
        [versionId]
    );

    const nodes = [
        ...basicConfigs.map((item) => ({
            client_node_id: item.client_config_id,
            parent_client_node_id: item.parent_client_config_id,
            engine_type: apiEngineType(item.engine_subtype),
            engine_subtype: item.engine_subtype,
            display_name: item.display_name,
            position_x: Number(item.position_x),
            position_y: Number(item.position_y),
            width: item.width === null ? null : Number(item.width),
            height: item.height === null ? null : Number(item.height),
            z_index: item.z_index,
            config: parseJsonColumn(item.config, {})
        })),
        ...flowConfigs.map((item) => {
            const config = parseJsonColumn(item.config, {});
            if (item.completion_condition) config.completion_condition = item.completion_condition;
            return {
                client_node_id: item.client_config_id,
                parent_client_node_id: item.parent_client_config_id,
                terminal_client_node_id: item.terminal_client_config_id,
                engine_type: 'flow',
                engine_subtype: item.engine_subtype,
                display_name: item.display_name || item.engine_name,
                position_x: Number(item.position_x),
                position_y: Number(item.position_y),
                width: item.width === null ? null : Number(item.width),
                height: item.height === null ? null : Number(item.height),
                z_index: item.z_index,
                config
            };
        })
    ].sort((first, second) => first.z_index - second.z_index);

    const membershipEdges = nodes
        .filter((node) => node.parent_client_node_id)
        .map((node, index) => {
            const parent = nodes.find((candidate) => candidate.client_node_id === node.parent_client_node_id);
            return {
                client_edge_id: `membership_${node.client_node_id}`,
                source_client_node_id: node.parent_client_node_id,
                target_client_node_id: node.client_node_id,
                flow_type: parent && parent.engine_subtype === 'parallel' ? 'parallel_child' : 'composite_child',
                completion_condition: null,
                sort_order: index,
                config: {}
            };
        });

    const edges = connectorConfigs.map((item) => {
        const config = parseJsonColumn(item.transition_config, item.transition_type === 'delay' ? { delay_seconds: 3 } : {});
        if (item.completion_condition) config.completion_condition = item.completion_condition;
        return {
            client_edge_id: item.client_config_id,
            source_client_node_id: item.source_client_config_id,
            target_client_node_id: item.destination_client_config_id,
            flow_type: item.engine_subtype === 'parallel' ? 'parallel' : 'sequential',
            completion_condition: item.completion_condition,
            sort_order: item.sequence_order,
            config
        };
    }).concat(membershipEdges);

    return {
        ...version,
        canvas_config: parseJsonColumn(version.canvas_config, {}),
        flow: { nodes, edges }
    };
}

const getEngineCatalog = async (_req, res) => {
    try {
        const [rows] = await db.query(
            `SELECT engine_id, engine_name, engine_type, engine_subtype, engine_description
             FROM engines WHERE is_active = 1 ORDER BY engine_id ASC`
        );
        return ok(res, rows.map((engine) => ({
            ...engine,
            engine_type: apiEngineType(engine.engine_subtype),
            symbol: engine.engine_subtype === 'sequential' ? '->'
                : engine.engine_subtype === 'parallel' ? '||'
                    : engine.engine_subtype === 'composite' ? 'rectangle' : null
        })));
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const uploadQuestMedia = async (req, res) => {
    if (!req.file) return fail(res, 'No media file uploaded', 400);
    return ok(res, {
        media_url: `/uploads/quests/${req.file.filename}`,
        mime_type: req.file.mimetype,
        size: req.file.size
    }, 'Media uploaded', 201);
};

const getQuestProblems = async (_req, res) => {
    try {
        const [rows] = await db.query(
            `SELECT id, title, parent_id, tree_level, is_leaf_node
             FROM problems
             ORDER BY tree_level ASC, title ASC`
        );
        return ok(res, rows.map((row) => ({
            ...row,
            is_leaf_node: Boolean(row.is_leaf_node)
        })));
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const listQuests = async (req, res) => {
    try {
        const status = req.query.status || null;
        const params = [];
        const conditions = [];

        const adminId = await getAdminId(req.user.user_id);
        if (!adminId) {
            const staffId = await getStaffId(req.user.user_id);
            if (!staffId) return fail(res, 'Staff account not found', 403);
            conditions.push('q.created_by_staff_id = ?');
            params.push(staffId);
        }

        if (status) {
            conditions.push('qv.status = ?');
            params.push(status);
        }

        const where = conditions.length ? `WHERE ${conditions.join(' AND ')}` : '';

        const [rows] = await db.query(
            `SELECT q.quest_id, q.quest_title, q.quest_description, q.quest_level,
                    q.problem_id, problem.title AS problem_title,
                    CONCAT_WS(' > ', root_problem.title, parent_problem.title, problem.title) AS problem_path,
                    qv.status AS approval_status,
                    q.is_active, q.created_at, qv.reviewed_at, qv.review_note,
                    qv.version_id AS latest_version_id, qv.version_number AS latest_version_number
             FROM quests q
             LEFT JOIN problems problem ON problem.id = q.problem_id
             LEFT JOIN problems parent_problem ON parent_problem.id = problem.parent_id
             LEFT JOIN problems root_problem ON root_problem.id = parent_problem.parent_id
             LEFT JOIN quest_versions qv ON qv.version_id = (
                SELECT qv2.version_id
                FROM quest_versions qv2
                WHERE qv2.quest_id = q.quest_id
                ORDER BY qv2.version_number DESC
                LIMIT 1
             )
             ${where}
             ORDER BY q.created_at DESC`,
            params
        );

        return ok(res, rows.map((row) => ({
            ...row,
            is_active: Boolean(row.is_active)
        })));
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const listApprovedQuestCatalog = async (req, res) => {
    try {
        const studentId = await getStudentId(req.user.user_id);
        const problemId = String(req.query.problem_id || '').trim();
        // Param đầu tiên phục vụ subquery is_completed (theo student hiện tại).
        const params = [studentId];
        let categoryFilter = '';
        if (problemId) {
            categoryFilter = `AND (q.problem_id = ? OR problem.parent_id = ?
                                   OR parent_problem.parent_id = ?)`;
            params.push(problemId, problemId, problemId);
        }

        const [rows] = await db.query(
            `SELECT q.quest_id, q.quest_title, q.quest_description, q.quest_level,
                    q.problem_id, problem.title AS problem_title,
                    CONCAT_WS(' > ', root_problem.title, parent_problem.title, problem.title) AS problem_path,
                    qv.status AS approval_status,
                    qv.version_id AS latest_version_id,
                    qv.version_number AS latest_version_number,
                    EXISTS(
                        SELECT 1 FROM quest_run_sessions r
                        WHERE r.quest_id = q.quest_id
                          AND r.student_id = ?
                          AND r.status = 'completed'
                    ) AS is_completed
             FROM quests q
             LEFT JOIN problems problem ON problem.id = q.problem_id
             LEFT JOIN problems parent_problem ON parent_problem.id = problem.parent_id
             LEFT JOIN problems root_problem ON root_problem.id = parent_problem.parent_id
             JOIN quest_versions qv ON qv.version_id = (
                SELECT qv2.version_id
                FROM quest_versions qv2
                WHERE qv2.quest_id = q.quest_id AND qv2.status = 'approved'
                ORDER BY qv2.version_number DESC
                LIMIT 1
             )
             WHERE q.is_active = 1
             ${categoryFilter}
             ORDER BY qv.reviewed_at DESC, q.created_at DESC`,
            params
        );

        // EXISTS trả 0/1 (NUMBER); model Android là boolean → ép kiểu để Gson parse đúng.
        rows.forEach(r => { r.is_completed = r.is_completed === 1 || r.is_completed === true; });

        return ok(res, rows);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const saveQuestDraft = async (req, res) => {
    const conn = await db.getConnection();
    try {
        const {
            quest_id,
            quest_title,
            quest_description,
            quest_level,
            problem_id,
            canvas_config,
            flow
        } = req.body;

        if (!quest_title || !quest_level) {
            return fail(res, 'Thiếu quest_title hoặc quest_level', 400);
        }

        const validationError = validateFlow(flow);
        if (validationError) return fail(res, validationError, 400);

        const staffId = await getStaffId(req.user.user_id);
        if (!staffId) return fail(res, 'Staff account not found', 403);

        // problem_id rỗng/null → QUEST TỔNG QUAN (không gán vấn đề). Nếu có → phải là lá Tầng 3.
        let normalizedProblemId = null;
        if (problem_id && typeof problem_id === 'string' && problem_id.trim()) {
            normalizedProblemId = problem_id.trim();
            const [[problem]] = await conn.query(
                `SELECT id FROM problems
                 WHERE id = ? AND tree_level = 3 AND is_leaf_node = 1`,
                [normalizedProblemId]
            );
            if (!problem) return fail(res, 'problem_id must reference a level-3 leaf problem', 400);
        }

        await conn.beginTransaction();

        let questId = quest_id || null;
        let versionNumber = 1;
        let versionId = null;

        if (questId) {
            const [[ownedQuest]] = await conn.query(
                `SELECT q.quest_id, latest.version_id, latest.version_number, latest.status
                 FROM quests q
                 LEFT JOIN quest_versions latest ON latest.version_id = (
                    SELECT candidate.version_id FROM quest_versions candidate
                    WHERE candidate.quest_id = q.quest_id
                    ORDER BY candidate.version_number DESC LIMIT 1
                 )
                 WHERE q.quest_id = ? AND q.created_by_staff_id = ?`,
                [questId, staffId]
            );
            if (!ownedQuest) {
                await conn.rollback();
                return fail(res, 'Quest not found or does not belong to this staff account', 404);
            }
            if (!['draft', 'rejected'].includes(ownedQuest.status)) {
                await conn.rollback();
                return fail(res, 'Only draft or rejected quests can be edited', 409);
            }
            await conn.query(
                `UPDATE quests
                 SET quest_title = ?, quest_description = ?, quest_level = ?,
                      problem_id = ?, is_active = 1
                 WHERE quest_id = ?`,
                [quest_title, quest_description || null, quest_level, normalizedProblemId, questId]
            );
            if (ownedQuest.status === 'draft') {
                versionId = ownedQuest.version_id;
                versionNumber = ownedQuest.version_number;
                await conn.query('DELETE FROM flow_engine_configs WHERE version_id = ?', [versionId]);
                await conn.query('DELETE FROM basic_engine_configs WHERE version_id = ?', [versionId]);
                await conn.query(
                    `UPDATE quest_versions SET canvas_config = ?, updated_at = CURRENT_TIMESTAMP
                     WHERE version_id = ?`,
                    [normalizeJson(canvas_config), versionId]
                );
            } else {
                versionNumber = ownedQuest.version_number + 1;
            }
        } else {
            const [questResult] = await conn.query(
                `INSERT INTO quests
                    (created_by_staff_id, problem_id, quest_title,
                     quest_description, quest_level, is_active)
                 VALUES (?, ?, ?, ?, ?, 1)`,
                [staffId, normalizedProblemId, quest_title, quest_description || null, quest_level]
            );
            questId = questResult.insertId;
        }

        if (!versionId) {
            const [versionResult] = await conn.query(
                `INSERT INTO quest_versions
                    (quest_id, version_number, status, canvas_config, created_by_staff_id)
                 VALUES (?, ?, 'draft', ?, ?)`,
                [questId, versionNumber, normalizeJson(canvas_config), staffId]
            );
            versionId = versionResult.insertId;
        }

        const [engineRows] = await conn.query(
            'SELECT engine_id, engine_type, engine_subtype FROM engines WHERE is_active = 1'
        );
        const engineBySubtype = new Map(engineRows.map((engine) => [engine.engine_subtype, engine]));
        const basicByClient = new Map();
        const flowByClient = new Map();
        const nodeByClient = new Map(flow.nodes.map((node) => [node.client_node_id, node]));

        for (const node of flow.nodes) {
            const definition = engineBySubtype.get(node.engine_subtype);
            if (!definition) throw new Error(`Engine definition not found: ${node.engine_subtype}`);
            if (definition.engine_type !== 'BASIC') continue;
            const completion = basicCompletion(node);
            const [result] = await conn.query(
                `INSERT INTO basic_engine_configs
                    (version_id, engine_id, client_config_id, display_name,
                     position_x, position_y, width, height, z_index,
                     config, completion_type, completion_config)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                [versionId, definition.engine_id, node.client_node_id, node.display_name || null,
                    node.position_x || 0, node.position_y || 0, node.width || null, node.height || null,
                    node.z_index || 0, normalizeJson(node.config), completion.type,
                    normalizeJson(completion.config, {})]
            );
            basicByClient.set(node.client_node_id, result.insertId);
        }

        for (const node of flow.nodes) {
            const definition = engineBySubtype.get(node.engine_subtype);
            if (!definition || definition.engine_type !== 'FLOW' || node.engine_subtype === 'sequential') continue;
            const completion = node.config && node.config.completion_condition;
            const [result] = await conn.query(
                `INSERT INTO flow_engine_configs
                    (version_id, engine_id, client_config_id, display_name,
                     position_x, position_y, width, height, z_index,
                     completion_condition, config)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                [versionId, definition.engine_id, node.client_node_id, node.display_name || null,
                    node.position_x || 0, node.position_y || 0, node.width || null, node.height || null,
                    node.z_index || 0, completion || null, normalizeJson(node.config)]
            );
            flowByClient.set(node.client_node_id, result.insertId);
        }

        const setParent = async (childClientId, parentClientId) => {
            const parentId = flowByClient.get(parentClientId);
            if (!parentId) return;
            if (basicByClient.has(childClientId)) {
                await conn.query(
                    'UPDATE basic_engine_configs SET parent_flow_config_id = ? WHERE basic_config_id = ?',
                    [parentId, basicByClient.get(childClientId)]
                );
            } else if (flowByClient.has(childClientId)) {
                await conn.query(
                    'UPDATE flow_engine_configs SET parent_flow_config_id = ? WHERE flow_config_id = ?',
                    [parentId, flowByClient.get(childClientId)]
                );
            }
        };

        for (const node of flow.nodes) {
            if (node.parent_client_node_id) await setParent(node.client_node_id, node.parent_client_node_id);
        }

        let generatedOrder = 0;
        for (const edge of flow.edges || []) {
            if (edge.flow_type !== 'sequential' && edge.flow_type !== 'parallel') {
                await setParent(edge.target_client_node_id, edge.source_client_node_id);
                continue;
            }
            const isParallelConnector = edge.flow_type === 'parallel';
            const definition = engineBySubtype.get(isParallelConnector ? 'parallel' : 'sequential');
            const sourceNode = nodeByClient.get(edge.source_client_node_id);
            const destinationNode = nodeByClient.get(edge.target_client_node_id);
            const sameParent = sourceNode && destinationNode
                && sourceNode.parent_client_node_id
                && sourceNode.parent_client_node_id === destinationNode.parent_client_node_id;
            const transitionConfig = edge.config || {};
            const transitionType = isParallelConnector ? null
                : ['tap', 'swipe', 'drag'].includes(transitionConfig.transition_type)
                ? transitionConfig.transition_type
                : Object.prototype.hasOwnProperty.call(transitionConfig, 'delay_seconds') ? 'delay' : 'immediate';
            const completionCondition = isParallelConnector
                ? (edge.completion_condition || transitionConfig.completion_condition || 'A_OR_B')
                : null;
            const sequenceOrder = isParallelConnector
                ? null
                : (edge.sort_order > 0 ? edge.sort_order : ++generatedOrder);
            await conn.query(
                `INSERT INTO flow_engine_configs
                    (version_id, engine_id, client_config_id, parent_flow_config_id,
                     source_basic_config_id, source_flow_config_id,
                     destination_basic_config_id, destination_flow_config_id,
                     sequence_order, transition_type, transition_config, completion_condition, config)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                [versionId, definition.engine_id,
                    edge.client_edge_id || `${isParallelConnector ? 'parallel' : 'sequential'}_${versionId}_${sequenceOrder || Date.now()}`,
                    sameParent ? flowByClient.get(sourceNode.parent_client_node_id) || null : null,
                    basicByClient.get(edge.source_client_node_id) || null,
                    flowByClient.get(edge.source_client_node_id) || null,
                    basicByClient.get(edge.target_client_node_id) || null,
                    flowByClient.get(edge.target_client_node_id) || null,
                    sequenceOrder, transitionType, normalizeJson(transitionConfig, {}),
                    completionCondition,
                    normalizeJson(transitionConfig, {})]
            );
        }

        for (const [clientId, flowId] of flowByClient.entries()) {
            const node = nodeByClient.get(clientId);
            if (!node || !['composite', 'quest'].includes(node.engine_subtype)) continue;
            const [[lastSequence]] = await conn.query(
                `SELECT destination_basic_config_id, destination_flow_config_id
                 FROM flow_engine_configs
                 WHERE parent_flow_config_id = ? AND sequence_order IS NOT NULL
                 ORDER BY sequence_order DESC LIMIT 1`,
                [flowId]
            );
            if (lastSequence) {
                await conn.query(
                    `UPDATE flow_engine_configs
                     SET terminal_basic_config_id = ?, terminal_flow_config_id = ?
                     WHERE flow_config_id = ?`,
                    [lastSequence.destination_basic_config_id, lastSequence.destination_flow_config_id, flowId]
                );
            }
        }

        await conn.commit();

        const savedVersion = await loadQuestVersion(versionId);
        return ok(res, savedVersion, 'Đã lưu bản nháp quest', 201);
    } catch (err) {
        await conn.rollback();
        return fail(res, 'Server error', 500, err.message);
    } finally {
        conn.release();
    }
};

const submitQuestForReview = async (req, res) => {
    try {
        const { questId } = req.params;
        const staffId = await getStaffId(req.user.user_id);
        if (!staffId) return fail(res, 'Staff account not found', 403);
        const [[version]] = await db.query(
            `SELECT qv.version_id
             FROM quest_versions qv
             JOIN quests q ON q.quest_id = qv.quest_id
             WHERE qv.quest_id = ? AND q.created_by_staff_id = ?
               AND qv.status = 'draft'
             ORDER BY qv.version_number DESC
             LIMIT 1`,
            [questId, staffId]
        );

        if (!version) return fail(res, 'Quest chưa có flow để gửi duyệt', 404);

        await db.query(
            `UPDATE quest_versions SET status = 'pending_review', submitted_at = CURRENT_TIMESTAMP
             WHERE version_id = ?`,
            [version.version_id]
        );
        return ok(res, { quest_id: Number(questId), version_id: version.version_id }, 'Đã gửi quest cho admin duyệt');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const reviewQuest = async (req, res) => {
    try {
        const { questId } = req.params;
        const { action, review_note } = req.body;

        if (!['approved', 'rejected'].includes(action)) {
            return fail(res, 'action phải là approved hoặc rejected', 400);
        }

        const [[admin]] = await db.query('SELECT admin_id FROM admins WHERE user_id = ?', [req.user.user_id]);
        if (!admin) return fail(res, 'Không tìm thấy admin', 403);

        const [[version]] = await db.query(
            `SELECT version_id FROM quest_versions
             WHERE quest_id = ? AND status = 'pending_review'
             ORDER BY version_number DESC
             LIMIT 1`,
            [questId]
        );

        if (!version) return fail(res, 'Quest chưa có phiên bản để duyệt', 404);

        await db.query(
            `UPDATE quest_versions
             SET status = ?, approved_at = IF(? = 'approved', CURRENT_TIMESTAMP, approved_at),
                 reviewed_by_admin_id = ?, reviewed_at = CURRENT_TIMESTAMP, review_note = ?
             WHERE version_id = ?`,
            [action, action, admin.admin_id, review_note || null, version.version_id]
        );

        return ok(res, { quest_id: Number(questId), status: action }, 'Đã cập nhật trạng thái duyệt quest');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const updateQuestVisibility = async (req, res) => {
    try {
        const questId = Number(req.params.questId);
        const isActive = req.body && req.body.is_active;
        if (!Number.isInteger(questId) || questId <= 0 || typeof isActive !== 'boolean') {
            return fail(res, 'questId and boolean is_active are required', 400);
        }

        const [result] = await db.query(
            'UPDATE quests SET is_active = ? WHERE quest_id = ?',
            [isActive ? 1 : 0, questId]
        );
        if (!result.affectedRows) return fail(res, 'Quest not found', 404);

        return ok(res, { quest_id: questId, is_active: isActive },
            isActive ? 'Quest restored' : 'Quest hidden');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getQuestVersion = async (req, res) => {
    try {
        const { versionId } = req.params;
        const version = await loadQuestVersion(versionId);
        if (!version) return fail(res, 'Không tìm thấy quest version', 404);
        if (!(await canReadQuest(req.user.user_id, version.quest_id))) {
            return fail(res, 'You cannot access this quest', 403);
        }
        return ok(res, version);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const deleteOwnDraftQuest = async (req, res) => {
    try {
        const questId = Number(req.params.questId);
        const staffId = await getStaffId(req.user.user_id);
        if (!staffId) return fail(res, 'Staff account not found', 403);

        const [[quest]] = await db.query(
            `SELECT q.quest_id,
                    SUM(CASE WHEN qv.status <> 'draft' THEN 1 ELSE 0 END) AS locked_versions
             FROM quests q
             LEFT JOIN quest_versions qv ON qv.quest_id = q.quest_id
             WHERE q.quest_id = ? AND q.created_by_staff_id = ?
             GROUP BY q.quest_id`,
            [questId, staffId]
        );
        if (!quest) return fail(res, 'Quest not found or does not belong to this staff account', 404);
        if (Number(quest.locked_versions) > 0) {
            return fail(res, 'Only draft quests can be deleted', 409);
        }

        await db.query(
            'DELETE FROM quests WHERE quest_id = ? AND created_by_staff_id = ?',
            [questId, staffId]
        );
        return ok(res, null, 'Draft quest deleted');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

async function closeStaleQuestRuns(staffId) {
    await db.query(
        `UPDATE quest_run_sessions rs
         JOIN quests q ON q.quest_id = rs.quest_id
         SET rs.status = 'abandoned',
             rs.completed_at = COALESCE(rs.completed_at, DATE_ADD(rs.started_at, INTERVAL 24 HOUR))
         WHERE q.created_by_staff_id = ?
           AND rs.status = 'in_progress'
           AND rs.started_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 HOUR)`,
        [staffId]
    );
}

const getOwnQuestMonthlyReport = async (req, res) => {
    try {
        const staffId = await getStaffId(req.user.user_id);
        if (!staffId) return fail(res, 'Staff account not found', 403);
        await closeStaleQuestRuns(staffId);
        const [rows] = await db.query(
            `SELECT DATE_FORMAT(rs.started_at, '%Y-%m') AS chart_month,
                    COUNT(rs.run_id) AS total_runs,
                    ROUND(SUM(rs.status = 'completed') * 100.0 / NULLIF(COUNT(rs.run_id), 0), 2) AS completion_rate,
                    ROUND(SUM(rs.status = 'abandoned') * 100.0 / NULLIF(COUNT(rs.run_id), 0), 2) AS abandonment_rate,
                    ROUND(AVG(CASE WHEN rs.status = 'completed' AND rs.completed_at IS NOT NULL
                        THEN TIMESTAMPDIFF(SECOND, rs.started_at, rs.completed_at) END) / 60.0, 2) AS avg_duration_minutes
             FROM quest_run_sessions rs
             JOIN quests q ON q.quest_id = rs.quest_id
             WHERE q.created_by_staff_id = ?
             GROUP BY DATE_FORMAT(rs.started_at, '%Y-%m')
             ORDER BY chart_month ASC`,
            [staffId]
        );
        return ok(res, rows);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getOwnQuestRankingReport = async (req, res) => {
    try {
        const staffId = await getStaffId(req.user.user_id);
        if (!staffId) return fail(res, 'Staff account not found', 403);
        await closeStaleQuestRuns(staffId);
        const [rows] = await db.query(
            `SELECT q.quest_id, q.quest_title,
                    COUNT(rs.run_id) AS total_runs,
                    SUM(CASE WHEN rs.status = 'completed' THEN 1 ELSE 0 END) AS total_completed
             FROM quests q
             LEFT JOIN quest_run_sessions rs ON rs.quest_id = q.quest_id
             WHERE q.created_by_staff_id = ?
             GROUP BY q.quest_id, q.quest_title
             ORDER BY total_completed DESC, total_runs DESC, q.quest_id DESC`,
            [staffId]
        );
        return ok(res, rows);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getOwnQuestEventReport = async (req, res) => {
    try {
        const staffId = await getStaffId(req.user.user_id);
        if (!staffId) return fail(res, 'Staff account not found', 403);
        await closeStaleQuestRuns(staffId);
        const [rows] = await db.query(
            `SELECT q.quest_id, q.quest_title, event_run.client_config_id,
                    COALESCE(MAX(basic.display_name), MAX(flow.display_name),
                             MAX(basic_engine.engine_name), MAX(flow_engine.engine_name),
                             event_run.client_config_id) AS node_name,
                    COALESCE(MAX(basic_engine.engine_subtype), MAX(flow_engine.engine_subtype), 'unknown') AS engine_subtype,
                    SUM(event_run.started_at IS NOT NULL) AS started_runs,
                    SUM(event_run.completed_at IS NOT NULL) AS completed_runs,
                    ROUND((SUM(event_run.started_at IS NOT NULL) - SUM(event_run.completed_at IS NOT NULL))
                        * 100.0 / NULLIF(SUM(event_run.started_at IS NOT NULL), 0), 2) AS drop_off_rate,
                    ROUND(AVG(CASE WHEN event_run.started_at IS NOT NULL AND event_run.completed_at IS NOT NULL
                        THEN TIMESTAMPDIFF(MICROSECOND, event_run.started_at, event_run.completed_at) / 1000000.0 END), 2)
                        AS avg_duration_seconds,
                    SUM(event_run.error_count) AS error_count
             FROM (
                 SELECT rs.run_id, rs.quest_id, rs.version_id, events.client_config_id,
                        MIN(CASE WHEN events.event_type = 'config_started' THEN
                            COALESCE(FROM_UNIXTIME(JSON_UNQUOTE(JSON_EXTRACT(events.payload, '$.client_timestamp_ms')) / 1000.0),
                                     events.created_at) END) AS started_at,
                        MAX(CASE WHEN events.event_type = 'config_completed' THEN
                            COALESCE(FROM_UNIXTIME(JSON_UNQUOTE(JSON_EXTRACT(events.payload, '$.client_timestamp_ms')) / 1000.0),
                                     events.created_at) END) AS completed_at,
                        SUM(events.event_type = 'error') AS error_count
                 FROM quest_run_sessions rs
                 JOIN quest_run_events events ON events.run_id = rs.run_id
                 WHERE events.client_config_id IS NOT NULL
                 GROUP BY rs.run_id, rs.quest_id, rs.version_id, events.client_config_id
             ) event_run
             JOIN quests q ON q.quest_id = event_run.quest_id
             LEFT JOIN basic_engine_configs basic
                    ON basic.version_id = event_run.version_id
                   AND basic.client_config_id = event_run.client_config_id
             LEFT JOIN engines basic_engine ON basic_engine.engine_id = basic.engine_id
             LEFT JOIN flow_engine_configs flow
                    ON flow.version_id = event_run.version_id
                   AND flow.client_config_id = event_run.client_config_id
                   AND flow.source_basic_config_id IS NULL
                   AND flow.source_flow_config_id IS NULL
                   AND flow.destination_basic_config_id IS NULL
                   AND flow.destination_flow_config_id IS NULL
             LEFT JOIN engines flow_engine ON flow_engine.engine_id = flow.engine_id
             WHERE q.created_by_staff_id = ?
             GROUP BY q.quest_id, q.quest_title, event_run.client_config_id
             ORDER BY q.quest_title, drop_off_rate DESC, avg_duration_seconds DESC`,
            [staffId]
        );
        return ok(res, rows);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getApprovedQuestFlow = async (req, res) => {
    try {
        const { questId } = req.params;
        const [[version]] = await db.query(
            `SELECT qv.version_id FROM quest_versions qv
             JOIN quests q ON q.quest_id = qv.quest_id
             WHERE qv.quest_id = ? AND qv.status = 'approved' AND q.is_active = 1
             ORDER BY qv.version_number DESC
             LIMIT 1`,
            [questId]
        );

        if (!version) return fail(res, 'Quest chưa được duyệt hoặc không tồn tại', 404);

        const data = await loadQuestVersion(version.version_id);
        return ok(res, data);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const startQuestRun = async (req, res) => {
    try {
        const { quest_id } = req.body;
        if (!quest_id) return fail(res, 'Thiếu quest_id', 400);

        const studentId = await getStudentId(req.user.user_id);
        if (!studentId) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [[version]] = await db.query(
            `SELECT qv.version_id FROM quest_versions qv
             JOIN quests q ON q.quest_id = qv.quest_id
             WHERE qv.quest_id = ? AND qv.status = 'approved' AND q.is_active = 1
             ORDER BY qv.version_number DESC
             LIMIT 1`,
            [quest_id]
        );
        if (!version) return fail(res, 'Quest chưa được duyệt', 404);

        const [result] = await db.query(
            `INSERT INTO quest_run_sessions
                (quest_id, version_id, student_id, status)
             VALUES (?, ?, ?, 'in_progress')`,
            [quest_id, version.version_id, studentId]
        );

        return ok(res, { run_id: result.insertId, version_id: version.version_id }, 'Đã bắt đầu quest', 201);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const appendQuestRunEvent = async (req, res) => {
    try {
        const { runId } = req.params;
        const { client_node_id, event_type, payload } = req.body;

        if (!event_type) return fail(res, 'Thiếu event_type', 400);

        const studentId = await getStudentId(req.user.user_id);
        if (!studentId) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [[run]] = await db.query(
            'SELECT run_id FROM quest_run_sessions WHERE run_id = ? AND student_id = ?',
            [runId, studentId]
        );
        if (!run) return fail(res, 'Không tìm thấy quest run của sinh viên hiện tại', 404);

        const storedEventType = event_type === 'node_started' ? 'config_started'
            : event_type === 'node_completed' ? 'config_completed' : event_type;
        const allowedEventTypes = new Set([
            'config_started', 'config_completed', 'input_received',
            'timer_finished', 'media_finished', 'error'
        ]);
        if (!allowedEventTypes.has(storedEventType)) {
            return fail(res, 'event_type không hợp lệ', 400);
        }

        await db.query(
            `INSERT INTO quest_run_events (run_id, client_config_id, event_type, payload)
             VALUES (?, ?, ?, ?)`,
            [runId, client_node_id || null, storedEventType, normalizeJson(payload, {})]
        );

        return ok(res, null, 'Đã ghi event');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const finishQuestRun = async (req, res) => {
    try {
        const { runId } = req.params;
        const { status, result_summary, effectiveness_rating, student_feedback } = req.body;
        const finalStatus = status || 'completed';

        if (!['completed', 'abandoned', 'failed'].includes(finalStatus)) {
            return fail(res, 'status phải là completed, abandoned hoặc failed', 400);
        }

        const studentId = await getStudentId(req.user.user_id);
        if (!studentId) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [[run]] = await db.query(
            'SELECT status FROM quest_run_sessions WHERE run_id = ? AND student_id = ?',
            [runId, studentId]
        );
        if (!run) return fail(res, 'Không tìm thấy quest run của sinh viên hiện tại', 404);
        if (run.status !== 'in_progress') {
            return fail(res, 'Quest run đã kết thúc', 409);
        }

        await db.query(
            `UPDATE quest_run_sessions
             SET status = ?, completed_at = CURRENT_TIMESTAMP, result_summary = ?,
                 effectiveness_rating = COALESCE(?, effectiveness_rating),
                 student_feedback = COALESCE(?, student_feedback)
             WHERE run_id = ?`,
            [finalStatus, normalizeJson(result_summary, {}),
                effectiveness_rating || null, student_feedback || null, runId]
        );

        return ok(res, null, 'Đã kết thúc quest run');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

module.exports = {
    getEngineCatalog,
    uploadQuestMedia,
    getQuestProblems,
    listQuests,
    listApprovedQuestCatalog,
    saveQuestDraft,
    submitQuestForReview,
    reviewQuest,
    updateQuestVisibility,
    getQuestVersion,
    deleteOwnDraftQuest,
    getOwnQuestMonthlyReport,
    getOwnQuestRankingReport,
    getOwnQuestEventReport,
    getApprovedQuestFlow,
    startQuestRun,
    appendQuestRunEvent,
    finishQuestRun
};
