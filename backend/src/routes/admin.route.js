const express = require('express');
const router = express.Router();
const authMiddleware = require('../middlewares/auth.middleware');
const adminMiddleware = require('../middlewares/admin.middleware');
const {
    getStudents, updateStudent, toggleStudentLock,
    getStaff, createStaff, updateStaff, toggleStaffLock,
    getCommunityReports, resolvePostReport, resolveCommentReport,
    getReviewRequests, resolveReviewRequest
} = require('../controllers/admin.controller');

router.use(authMiddleware);
router.use(adminMiddleware);

// Sinh viên
router.get('/students', getStudents);
router.put('/students/:studentId', updateStudent);
router.put('/students/:studentId/toggle-lock', toggleStudentLock);

// Nhân viên
router.get('/staff', getStaff);
router.post('/staff', createStaff);
router.put('/staff/:staffId', updateStaff);
router.put('/staff/:staffId/toggle-lock', toggleStaffLock);

// Quản lý cộng đồng
router.get('/community/reports', getCommunityReports);
router.post('/community/reports/post/:postId/resolve', resolvePostReport);
router.post('/community/reports/comment/:commentId/resolve', resolveCommentReport);
router.get('/community/review-requests', getReviewRequests);
router.post('/community/review-requests/:requestId/resolve', resolveReviewRequest);

module.exports = router;
