const express = require('express');
const router = express.Router();
const authMiddleware = require('../middlewares/auth.middleware');
const aichat = require('../controllers/aichat.controller');

// Tất cả route AI Chat đều yêu cầu đăng nhập.
router.use(authMiddleware);

// Mở UI: lời chào + gợi ý Tầng 2 (KHÔNG tạo session).
router.post('/sessions/start', aichat.startSession);

// Gửi 1 lượt chat (tạo session nếu session_id null).
router.post('/messages', aichat.sendMessage);

// Danh sách session của user (màn 2).
router.get('/sessions', aichat.getSessions);

// Load lại 1 session (trả chat_history).
router.get('/sessions/:id', aichat.getSession);

// Kết thúc session → sinh title.
router.post('/sessions/:id/end', aichat.endSession);

// User chọn vấn đề ưu tiên sau popup select_priority.
router.post('/sessions/:id/priority', aichat.pickPriority);

// User chọn hướng ở lượt cuối 4.2 (popup select_route): "quest" hoặc "community".
router.post('/sessions/:id/route', aichat.pickRoute);

module.exports = router;
