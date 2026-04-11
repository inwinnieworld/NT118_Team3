const db = require('../config/db');
const bcrypt = require('bcryptjs');

// ==================== SINH VIÊN ====================

// GET /api/admin/students?page=1&search=
const getStudents = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = 10;
        const offset = (page - 1) * limit;
        const search = req.query.search ? `%${req.query.search}%` : '%';

        const [rows] = await db.query(`
            SELECT u.user_id, u.name, u.email, u.phone, u.avatar_url, u.is_locked,
                   s.student_id, s.student_code, s.major, s.faculty, s.year_of_study
            FROM USERS u
            JOIN STUDENTS s ON u.user_id = s.user_id
            WHERE (u.name LIKE ? OR s.student_code LIKE ? OR u.email LIKE ?)
            ORDER BY u.created_at DESC
            LIMIT ? OFFSET ?
        `, [search, search, search, limit, offset]);

        const [[{ total }]] = await db.query(`
            SELECT COUNT(*) as total FROM USERS u
            JOIN STUDENTS s ON u.user_id = s.user_id
            WHERE (u.name LIKE ? OR s.student_code LIKE ? OR u.email LIKE ?)
        `, [search, search, search]);

        res.json({ data: rows, total, page, totalPages: Math.ceil(total / limit) });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PUT /api/admin/students/:studentId
const updateStudent = async (req, res) => {
    try {
        const { studentId } = req.params;
        const { name, email, phone, major, faculty, year_of_study } = req.body;

        const [[student]] = await db.query(
            'SELECT user_id FROM STUDENTS WHERE student_id = ?', [studentId]
        );
        if (!student) return res.status(404).json({ message: 'Student not found' });

        await db.query(
            'UPDATE USERS SET name = ?, email = ?, phone = ? WHERE user_id = ?',
            [name, email, phone, student.user_id]
        );
        await db.query(
            'UPDATE STUDENTS SET major = ?, faculty = ?, year_of_study = ? WHERE student_id = ?',
            [major, faculty, year_of_study, studentId]
        );

        res.json({ message: 'Cập nhật thành công' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PUT /api/admin/students/:studentId/toggle-lock
const toggleStudentLock = async (req, res) => {
    try {
        const { studentId } = req.params;
        const [[row]] = await db.query(
            `SELECT u.user_id, u.is_locked FROM USERS u
             JOIN STUDENTS s ON u.user_id = s.user_id
             WHERE s.student_id = ?`, [studentId]
        );
        if (!row) return res.status(404).json({ message: 'Student not found' });

        const newStatus = row.is_locked ? 0 : 1;
        await db.query('UPDATE USERS SET is_locked = ? WHERE user_id = ?', [newStatus, row.user_id]);

        res.json({
            message: newStatus ? 'Đã vô hiệu hóa tài khoản' : 'Đã kích hoạt tài khoản',
            is_locked: !!newStatus
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// ==================== NHÂN VIÊN ====================

// GET /api/admin/staff?page=1&search=
const getStaff = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = 10;
        const offset = (page - 1) * limit;
        const search = req.query.search ? `%${req.query.search}%` : '%';

        const [rows] = await db.query(`
            SELECT u.user_id, u.name, u.email, u.phone, u.avatar_url, u.is_locked,
                   st.staff_id, st.position, st.department, st.hire_date
            FROM USERS u
            JOIN STAFF st ON u.user_id = st.user_id
            WHERE (u.name LIKE ? OR u.email LIKE ?)
            ORDER BY st.hire_date DESC
            LIMIT ? OFFSET ?
        `, [search, search, limit, offset]);

        const [[{ total }]] = await db.query(`
            SELECT COUNT(*) as total FROM USERS u
            JOIN STAFF st ON u.user_id = st.user_id
            WHERE (u.name LIKE ? OR u.email LIKE ?)
        `, [search, search]);

        res.json({ data: rows, total, page, totalPages: Math.ceil(total / limit) });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST /api/admin/staff  — Thêm nhân viên mới
const createStaff = async (req, res) => {
    try {
        const { name, email, password, phone, position, department } = req.body;

        if (!name || !email || !password)
            return res.status(400).json({ message: 'Vui lòng nhập đầy đủ thông tin' });

        const [existing] = await db.query('SELECT user_id FROM USERS WHERE email = ?', [email]);
        if (existing.length > 0)
            return res.status(400).json({ message: 'Email đã tồn tại' });

        const hash = await bcrypt.hash(password, 10);
        const [result] = await db.query(
            'INSERT INTO USERS (name, email, password_hash, phone) VALUES (?, ?, ?, ?)',
            [name, email, hash, phone]
        );

        await db.query(
            'INSERT INTO STAFF (user_id, position, department, hire_date) VALUES (?, ?, ?, CURDATE())',
            [result.insertId, position, department]
        );

        res.status(201).json({ message: 'Tạo tài khoản nhân viên thành công' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PUT /api/admin/staff/:staffId
const updateStaff = async (req, res) => {
    try {
        const { staffId } = req.params;
        const { name, email, phone, position, department } = req.body;

        const [[staff]] = await db.query(
            'SELECT user_id FROM STAFF WHERE staff_id = ?', [staffId]
        );
        if (!staff) return res.status(404).json({ message: 'Staff not found' });

        await db.query(
            'UPDATE USERS SET name = ?, email = ?, phone = ? WHERE user_id = ?',
            [name, email, phone, staff.user_id]
        );
        await db.query(
            'UPDATE STAFF SET position = ?, department = ? WHERE staff_id = ?',
            [position, department, staffId]
        );

        res.json({ message: 'Cập nhật thành công' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PUT /api/admin/staff/:staffId/toggle-lock
const toggleStaffLock = async (req, res) => {
    try {
        const { staffId } = req.params;
        const [[row]] = await db.query(
            `SELECT u.user_id, u.is_locked FROM USERS u
             JOIN STAFF st ON u.user_id = st.user_id
             WHERE st.staff_id = ?`, [staffId]
        );
        if (!row) return res.status(404).json({ message: 'Staff not found' });

        const newStatus = row.is_locked ? 0 : 1;
        await db.query('UPDATE USERS SET is_locked = ? WHERE user_id = ?', [newStatus, row.user_id]);

        res.json({
            message: newStatus ? 'Đã vô hiệu hóa tài khoản' : 'Đã kích hoạt tài khoản',
            is_locked: !!newStatus
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

module.exports = {
    getStudents, updateStudent, toggleStudentLock,
    getStaff, createStaff, updateStaff, toggleStaffLock
};
