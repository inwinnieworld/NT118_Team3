const express = require('express');
const router = express.Router();
const authMiddleware = require('../middlewares/auth.middleware');
const adminMiddleware = require('../middlewares/admin.middleware');
const {
    getStudents, updateStudent, toggleStudentLock,
    getStaff, createStaff, updateStaff, toggleStaffLock
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

module.exports = router;
