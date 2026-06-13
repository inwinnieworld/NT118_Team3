const db = require('../config/db');
const { isValidEmail, isValidPhone } = require('../utils/validate');
const { ok, fail } = require('../utils/response');

// GET /api/profile
const getProfile = async (req, res) => {
    try {
        const userId = req.user.user_id;

        const [rows] = await db.query(`
            SELECT 
                u.user_id, u.name, u.email, u.phone, u.avatar_url,
                s.student_id, s.student_code, s.major, s.faculty, s.year_of_study,
                s.emergency_phone
            FROM users u
            JOIN students s ON u.user_id = s.user_id
            WHERE u.user_id = ?
            LIMIT 1
        `, [userId]);

        if (rows.length === 0) return res.status(404).json({ message: 'User not found' });

        return ok(res, rows[0], 'Profile retrieved successfully');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// PUT /api/profile
const updateProfile = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { name, phone, major, faculty, year_of_study, emergency_phone } = req.body;

        if (phone && !isValidPhone(phone))
            return res.status(400).json({ message: 'Số điện thoại phải đúng 10 chữ số' });

        if (emergency_phone && !isValidPhone(emergency_phone))
            return res.status(400).json({ message: 'Số điện thoại khẩn cấp phải đúng 10 chữ số' });

        await db.query(
            `UPDATE users SET name = ?, phone = ? WHERE user_id = ?`,
            [name, phone, userId]
        );

        await db.query(
            `UPDATE students SET major = ?, faculty = ?, year_of_study = ?, emergency_phone = ? WHERE user_id = ?`,
            [major, faculty, year_of_study, emergency_phone, userId]
        );

        res.json({ message: 'Profile updated successfully' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST /api/profile/avatar
const uploadAvatar = async (req, res) => {
    try {
        if (!req.file) return res.status(400).json({ message: 'No file uploaded' });

        const userId = req.user.user_id;
        const avatarUrl = `/uploads/avatars/${req.file.filename}`;

        await db.query(
            `UPDATE users SET avatar_url = ? WHERE user_id = ?`,
            [avatarUrl, userId]
        );

        res.json({ message: 'Avatar uploaded', avatar_url: avatarUrl });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PUT /api/profile/change-password
const changePassword = async (req, res) => {
    const bcrypt = require('bcryptjs');
    try {
        const userId = req.user.user_id;
        const { old_password, new_password } = req.body;

        if (!old_password || !new_password)
            return res.status(400).json({ message: 'Vui lòng nhập đầy đủ thông tin' });

        if (new_password.length < 6)
            return res.status(400).json({ message: 'Mật khẩu mới phải có ít nhất 6 ký tự' });

        const [rows] = await db.query('SELECT password_hash FROM users WHERE user_id = ?', [userId]);
        if (rows.length === 0) return res.status(404).json({ message: 'User not found' });

        const isMatch = await bcrypt.compare(old_password, rows[0].password_hash);
        if (!isMatch) return res.status(400).json({ message: 'Mật khẩu cũ không đúng' });

        const newHash = await bcrypt.hash(new_password, 10);
        await db.query('UPDATE users SET password_hash = ? WHERE user_id = ?', [newHash, userId]);

        res.json({ message: 'Đổi mật khẩu thành công' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

module.exports = { getProfile, updateProfile, uploadAvatar, changePassword };
