const db = require('../config/db');

module.exports = async (req, res, next) => {
    try {
        const [rows] = await db.query(
            `SELECT u.user_id, u.role, st.staff_id, a.admin_id
             FROM users u
             LEFT JOIN staff st ON u.user_id = st.user_id
             LEFT JOIN admins a ON u.user_id = a.user_id
             WHERE u.user_id = ?`,
            [req.user.user_id]
        );

        const account = rows[0];
        if (!account || (!account.staff_id && !account.admin_id)) {
            return res.status(403).json({ message: 'Access denied: Staff or admins only' });
        }

        req.staff_id = account.staff_id || null;
        req.admin_id = account.admin_id || null;
        next();
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};
