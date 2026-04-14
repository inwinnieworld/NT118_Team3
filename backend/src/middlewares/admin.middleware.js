const db = require('../config/db');

// Kiểm tra user có phải admin không
module.exports = async (req, res, next) => {
    try {
        const [rows] = await db.query(
            'SELECT admin_id FROM ADMINS WHERE user_id = ?',
            [req.user.user_id]
        );
        if (rows.length === 0) {
            return res.status(403).json({ message: 'Access denied: Admins only' });
        }
        next();
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};
