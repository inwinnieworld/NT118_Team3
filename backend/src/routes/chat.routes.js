const express = require('express');
const router = express.Router();

const authMiddleware = require('../middlewares/auth.middleware');
const {
    getMessagesWithUser,
    getConversations
} = require('../controllers/chat.controller');

router.get('/ping', (req, res) => {
    res.json({
        success: true,
        message: 'chat route ok'
    });
});

router.use(authMiddleware);

// API lấy danh sách người đã nhắn tin
router.get('/conversations', getConversations);

// API lấy đoạn hội thoại với 1 người
router.get('/with/:studentId', getMessagesWithUser);

module.exports = router;