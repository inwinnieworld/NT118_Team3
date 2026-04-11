const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('../config/db');

// POST /api/auth/login
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        const [rows] = await db.query(
            'SELECT * FROM USERS WHERE email = ?',
            [email]
        );

        if (rows.length === 0)
            return res.status(401).json({ message: 'Email hoặc mật khẩu không đúng' });

        const user = rows[0];

        if (user.is_locked)
            return res.status(403).json({ message: 'Tài khoản đã bị vô hiệu hóa' });

        const isMatch = await bcrypt.compare(password, user.password_hash);
        if (!isMatch)
            return res.status(401).json({ message: 'Email hoặc mật khẩu không đúng' });

        // Xác định role
        let role = 'student';
        const [[adminRow]] = await db.query(
            'SELECT admin_id FROM ADMINS WHERE user_id = ?', [user.user_id]
        );
        if (adminRow) {
            role = 'admin';
        } else {
            const [[staffRow]] = await db.query(
                'SELECT staff_id FROM STAFF WHERE user_id = ?', [user.user_id]
            );
            if (staffRow) role = 'staff';
        }

        const token = jwt.sign(
            { user_id: user.user_id, email: user.email, role },
            process.env.JWT_SECRET,
            { expiresIn: '7d' }
        );

        res.json({
            token,
            role,
            user: {
                user_id: user.user_id,
                name: user.name,
                email: user.email,
                avatar_url: user.avatar_url
            }
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
});

module.exports = router;
