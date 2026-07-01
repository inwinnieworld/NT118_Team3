const aichatService = require('../services/aichat.service');
const { ok, fail } = require('../utils/response');

/**
 * Controller AI Chat (Dr.Bug) — tầng HTTP: validate input, gọi service, trả ok/fail.
 * Mọi logic nghiệp vụ nằm ở aichat.service.js.
 */

// POST /api/aichat/sessions/start — mở UI: trả lời chào + gợi ý Tầng 2. KHÔNG tạo session.
async function startSession(req, res) {
    try {
        const data = await aichatService.startSession(req.user.user_id);
        return ok(res, data);
    } catch (err) {
        console.error('[AICHAT] startSession:', err.message);
        return fail(res, err.message, 500);
    }
}

// POST /api/aichat/messages — gửi 1 lượt chat. Tạo session nếu session_id null.
async function sendMessage(req, res) {
    try {
        const { session_id, text, picked_problem_id } = req.body;
        if (!text || !text.trim()) return fail(res, 'Nội dung tin nhắn không được rỗng', 400);

        const data = await aichatService.handleMessage({
            userId: req.user.user_id,
            sessionId: session_id || null,
            text,
            pickedProblemId: picked_problem_id || null
        });
        return ok(res, data);
    } catch (err) {
        if (err.code === 'GROK_NOT_CONFIGURED') {
            return fail(res, 'Trợ lý AI chưa được cấu hình (thiếu GROQ_API_KEY)', 503);
        }
        console.error('[AICHAT] sendMessage:', err.message);
        return fail(res, err.message, 500);
    }
}

// POST /api/aichat/sessions/:id/priority — user chọn vấn đề ưu tiên sau popup select_priority.
async function pickPriority(req, res) {
    try {
        const { problem_id } = req.body;
        if (!problem_id) return fail(res, 'Thiếu problem_id', 400);

        const data = await aichatService.pickPriority({
            userId: req.user.user_id,
            sessionId: Number(req.params.id),
            problemId: problem_id
        });
        return ok(res, data);
    } catch (err) {
        console.error('[AICHAT] pickPriority:', err.message);
        return fail(res, err.message, 500);
    }
}

// POST /api/aichat/sessions/:id/route — user chọn hướng ở popup lượt cuối 4.2 (quest | community).
async function pickRoute(req, res) {
    try {
        const { route } = req.body;
        if (route !== 'quest' && route !== 'community') {
            return fail(res, 'route phải là "quest" hoặc "community"', 400);
        }

        const data = await aichatService.pickRoute({
            userId: req.user.user_id,
            sessionId: Number(req.params.id),
            routeKey: route
        });
        return ok(res, data);
    } catch (err) {
        console.error('[AICHAT] pickRoute:', err.message);
        return fail(res, err.message, 500);
    }
}

// GET /api/aichat/sessions — danh sách session của user (cho màn 2).
async function getSessions(req, res) {
    try {
        const data = await aichatService.getSessions(req.user.user_id);
        return ok(res, data);
    } catch (err) {
        console.error('[AICHAT] getSessions:', err.message);
        return fail(res, err.message, 500);
    }
}

// GET /api/aichat/sessions/:id — load lại 1 session (trả chat_history để UI render).
async function getSession(req, res) {
    try {
        const data = await aichatService.getSession(req.user.user_id, Number(req.params.id));
        return ok(res, data);
    } catch (err) {
        console.error('[AICHAT] getSession:', err.message);
        return fail(res, err.message, 404);
    }
}

// POST /api/aichat/sessions/:id/end — kết thúc session → sinh title.
async function endSession(req, res) {
    try {
        const data = await aichatService.endSession(req.user.user_id, Number(req.params.id));
        return ok(res, data);
    } catch (err) {
        console.error('[AICHAT] endSession:', err.message);
        return fail(res, err.message, 500);
    }
}

module.exports = {
    startSession,
    sendMessage,
    pickPriority,
    pickRoute,
    getSessions,
    getSession,
    endSession
};
