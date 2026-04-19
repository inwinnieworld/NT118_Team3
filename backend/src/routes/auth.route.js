const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('../config/db');
const { ok, fail } = require('../utils/response');

// POST /api/auth/login
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        const [rows] = await db.query('SELECT * FROM USERS WHERE email = ?', [email]);

        if (rows.length === 0)
            return fail(res, 'Email hoặc mật khẩu không đúng', 401);

        const user = rows[0];

        if (user.is_locked)
            return fail(res, 'Tài khoản đã bị vô hiệu hóa', 403);

        const isMatch = await bcrypt.compare(password, user.password_hash);
        if (!isMatch)
            return fail(res, 'Email hoặc mật khẩu không đúng', 401);

        // Xác định role
        let role = 'STUDENT';
        let studentCode = null;
        let studentId = null;

        const [[adminRow]] = await db.query('SELECT admin_id, admin_role FROM ADMINS WHERE user_id = ?', [user.user_id]);
        if (adminRow) {
            role = 'ADMIN';
        } else {
            const [[staffRow]] = await db.query('SELECT staff_id, position FROM STAFF WHERE user_id = ?', [user.user_id]);
            if (staffRow) {
                role = 'STAFF';
            } else {
                const [[studentRow]] = await db.query('SELECT student_id, student_code FROM STUDENTS WHERE user_id = ?', [user.user_id]);
                if (studentRow) {
                    studentCode = studentRow.student_code;
                    studentId = studentRow.student_id;
                }
            }
        }

        const token = jwt.sign(
            { user_id: user.user_id, email: user.email, role },
            process.env.JWT_SECRET,
            { expiresIn: '7d' }
        );

        return ok(res, {
            token,
            user: {
                userId: user.user_id,
                name: user.name,
                email: user.email,
                phone: user.phone,
                avatarUrl: user.avatar_url,
                role,
                studentCode,
                studentId
            }
        }, 'Đăng nhập thành công');

    } catch (err) {
        return fail(res, 'Server error: ' + err.message);
    }
});

module.exports = router;
