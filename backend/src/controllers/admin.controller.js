

const db = require('../config/db');
const bcrypt = require('bcryptjs');
const { isValidEmail, isValidPhone } = require('../utils/validate');

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

        if (email && !isValidEmail(email))
            return res.status(400).json({ message: 'Email không đúng định dạng' });

        if (phone && !isValidPhone(phone))
            return res.status(400).json({ message: 'Số điện thoại phải đúng 10 chữ số' });

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

        if (!isValidEmail(email))
            return res.status(400).json({ message: 'Email không đúng định dạng' });

        if (!isValidPhone(phone))
            return res.status(400).json({ message: 'Số điện thoại phải đúng 10 chữ số' });

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

        if (email && !isValidEmail(email))
            return res.status(400).json({ message: 'Email không đúng định dạng' });

        if (phone && !isValidPhone(phone))
            return res.status(400).json({ message: 'Số điện thoại phải đúng 10 chữ số' });

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

// ==================== QUẢN LÝ CỘNG ĐỒNG ====================

const notifyStudent = async (recipientStudentId, type, title, body, relatedPostId = null, relatedCommentId = null) => {
    await db.query(`
        INSERT INTO community_notifications
            (recipient_student_id, type, title, body, related_post_id, related_comment_id)
        VALUES (?, ?, ?, ?, ?, ?)
    `, [recipientStudentId, type, title, body || null, relatedPostId, relatedCommentId]);
};

const resolveAdminId = async (userId) => {
    const [[admin]] = await db.query('SELECT admin_id FROM admins WHERE user_id = ? LIMIT 1', [userId]);
    return admin ? admin.admin_id : null;
};

// GET /api/admin/community/reports?type=post|comment&status=pending
const getCommunityReports = async (req, res) => {
    try {
        const status = req.query.status || 'pending';
        const type = req.query.type || 'all';

        let postReports = [];
        let commentReports = [];

        if (type === 'post' || type === 'all') {
            const [rows] = await db.query(`
                SELECT
                    pr.post_id,
                    MIN(pr.report_id) AS report_id,
                    cp.title,
                    cp.content,
                    cp.is_hidden,
                    cp.student_id AS author_student_id,
                    au.name AS author_name,
                    COUNT(*) AS report_count,
                    MAX(pr.created_at) AS last_reported_at,
                    GROUP_CONCAT(DISTINCT COALESCE(pr.report_detail, pr.report_reason) SEPARATOR ' | ') AS reasons
                FROM post_reports pr
                JOIN community_posts cp ON pr.post_id = cp.post_id
                LEFT JOIN students ast ON cp.student_id = ast.student_id
                LEFT JOIN users au ON ast.user_id = au.user_id
                WHERE pr.status = ?
                GROUP BY pr.post_id
                ORDER BY report_count DESC, last_reported_at DESC
            `, [status]);
            postReports = rows;
        }

        if (type === 'comment' || type === 'all') {
            const [rows] = await db.query(`
                SELECT
                    cr.comment_id,
                    MIN(cr.report_id) AS report_id,
                    c.content,
                    c.is_hidden,
                    c.post_id,
                    c.student_id AS author_student_id,
                    au.name AS author_name,
                    COUNT(*) AS report_count,
                    MAX(cr.created_at) AS last_reported_at,
                    GROUP_CONCAT(DISTINCT COALESCE(cr.report_detail, cr.report_reason) SEPARATOR ' | ') AS reasons
                FROM comment_reports cr
                JOIN comments c ON cr.comment_id = c.comment_id
                LEFT JOIN students ast ON c.student_id = ast.student_id
                LEFT JOIN users au ON ast.user_id = au.user_id
                WHERE cr.status = ?
                GROUP BY cr.comment_id
                ORDER BY report_count DESC, last_reported_at DESC
            `, [status]);
            commentReports = rows;
        }

        res.json({ success: true, data: { postReports, commentReports } });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST /api/admin/community/reports/post/:postId/resolve  body {action}
const resolvePostReport = async (req, res) => {
    try {
        const { postId } = req.params;
        const { action } = req.body; // accept | reject
        const adminId = await resolveAdminId(req.user.user_id);

        const [[post]] = await db.query(
            'SELECT post_id, student_id, title FROM community_posts WHERE post_id = ?',
            [postId]
        );
        if (!post) return res.status(404).json({ message: 'Không tìm thấy bài viết' });

        const newStatus = action === 'accept' ? 'resolved' : 'dismissed';

        await db.query(
            'UPDATE post_reports SET status = ?, reviewed_at = CURRENT_TIMESTAMP, reviewed_by_admin_id = ? WHERE post_id = ? AND status = \'pending\'',
            [newStatus, adminId, postId]
        );

        if (action === 'accept') {
            await db.query(
                'UPDATE community_posts SET is_hidden = 1, hidden_at = CURRENT_TIMESTAMP WHERE post_id = ?',
                [postId]
            );
            await notifyStudent(
                post.student_id,
                'post_hidden',
                'Bài viết của bạn đã bị ẩn',
                'Bài viết "' + (post.title || '') + '" đã bị ẩn do vi phạm quy định cộng đồng. Bạn có thể gửi yêu cầu xem xét lại.',
                post.post_id
            );
        }

        res.json({ success: true, message: action === 'accept' ? 'Đã ẩn bài viết' : 'Đã từ chối báo cáo' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST /api/admin/community/reports/comment/:commentId/resolve  body {action}
const resolveCommentReport = async (req, res) => {
    try {
        const { commentId } = req.params;
        const { action } = req.body;
        const adminId = await resolveAdminId(req.user.user_id);

        const [[comment]] = await db.query(
            'SELECT comment_id, student_id, post_id FROM comments WHERE comment_id = ?',
            [commentId]
        );
        if (!comment) return res.status(404).json({ message: 'Không tìm thấy bình luận' });

        const newStatus = action === 'accept' ? 'resolved' : 'dismissed';

        await db.query(
            'UPDATE comment_reports SET status = ?, reviewed_at = CURRENT_TIMESTAMP, reviewed_by_admin_id = ? WHERE comment_id = ? AND status = \'pending\'',
            [newStatus, adminId, commentId]
        );

        if (action === 'accept') {
            await db.query('UPDATE comments SET is_hidden = 1 WHERE comment_id = ?', [commentId]);
            await notifyStudent(
                comment.student_id,
                'post_hidden',
                'Bình luận của bạn đã bị ẩn',
                'Một bình luận của bạn đã bị ẩn do vi phạm quy định cộng đồng.',
                comment.post_id,
                comment.comment_id
            );
        }

        res.json({ success: true, message: action === 'accept' ? 'Đã ẩn bình luận' : 'Đã từ chối báo cáo' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// GET /api/admin/community/review-requests?status=pending
const getReviewRequests = async (req, res) => {
    try {
        const status = req.query.status || 'pending';
        const [rows] = await db.query(`
            SELECT
                rr.request_id, rr.post_id, rr.message, rr.status, rr.created_at,
                cp.title, cp.content, cp.is_hidden,
                rr.student_id,
                au.name AS author_name
            FROM post_review_requests rr
            JOIN community_posts cp ON rr.post_id = cp.post_id
            LEFT JOIN students ast ON rr.student_id = ast.student_id
            LEFT JOIN users au ON ast.user_id = au.user_id
            WHERE rr.status = ?
            ORDER BY rr.created_at DESC
        `, [status]);

        res.json({ success: true, data: { requests: rows } });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST /api/admin/community/review-requests/:requestId/resolve  body {action}
const resolveReviewRequest = async (req, res) => {
    try {
        const { requestId } = req.params;
        const { action } = req.body;
        const adminId = await resolveAdminId(req.user.user_id);

        const [[request]] = await db.query(
            'SELECT rr.request_id, rr.post_id, rr.student_id, cp.title FROM post_review_requests rr JOIN community_posts cp ON rr.post_id = cp.post_id WHERE rr.request_id = ?',
            [requestId]
        );
        if (!request) return res.status(404).json({ message: 'Không tìm thấy yêu cầu' });

        const newStatus = action === 'accept' ? 'accepted' : 'rejected';

        await db.query(
            'UPDATE post_review_requests SET status = ?, reviewed_at = CURRENT_TIMESTAMP, reviewed_by_admin_id = ? WHERE request_id = ?',
            [newStatus, adminId, requestId]
        );

        if (action === 'accept') {
            await db.query(
                'UPDATE community_posts SET is_hidden = 0, hidden_at = NULL WHERE post_id = ?',
                [request.post_id]
            );
            // Gỡ luôn các report đã resolved của bài này để không bị lọc lại.
            await db.query(
                'UPDATE post_reports SET status = \'dismissed\' WHERE post_id = ? AND status = \'resolved\'',
                [request.post_id]
            );
            await notifyStudent(
                request.student_id,
                'post_restored',
                'Bài viết của bạn đã được khôi phục',
                'Bài viết "' + (request.title || '') + '" đã được hiển thị lại sau khi xem xét.',
                request.post_id
            );
        } else {
            await notifyStudent(
                request.student_id,
                'review_result',
                'Yêu cầu xem xét bị từ chối',
                'Yêu cầu xem xét cho bài viết "' + (request.title || '') + '" đã bị từ chối. Bài viết vẫn bị ẩn.',
                request.post_id
            );
        }

        res.json({ success: true, message: 'Đã xử lý yêu cầu xem xét' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

module.exports = {
    getStudents, updateStudent, toggleStudentLock,
    getStaff, createStaff, updateStaff, toggleStaffLock,
    getCommunityReports, resolvePostReport, resolveCommentReport,
    getReviewRequests, resolveReviewRequest
};
