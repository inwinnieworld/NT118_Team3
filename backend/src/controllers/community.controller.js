const db = require('../config/db');
const { ok, fail } = require('../utils/response');

// GET /api/community/posts?filter=new&page=1
const getPosts = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = 20;
        const offset = (page - 1) * limit;
        const filter = req.query.filter || 'new'; // new | trending | best | my_logs
        const search = req.query.search ? `%${req.query.search}%` : '%';
        const userId = req.user.user_id;

        let orderBy = 'cp.created_at DESC';
        let extraWhere = '';

        if (filter === 'trending') {
            orderBy = 'upvote_count DESC, cp.created_at DESC';
        } else if (filter === 'best') {
            orderBy = 'comment_count DESC, upvote_count DESC';
        } else if (filter === 'my_logs') {
            extraWhere += ` AND s.user_id = ${userId}`;
        }

        const [rows] = await db.query(`
            SELECT 
                cp.post_id,
                cp.title,
                cp.content,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,
                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.avatar_url END AS author_avatar,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'UPVOTE' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'DOWNVOTE' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                EXISTS(SELECT 1 FROM saved_posts sp2 WHERE sp2.post_id = cp.post_id AND sp2.student_id = (SELECT student_id FROM students WHERE user_id = ${userId} LIMIT 1)) AS is_saved
            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            WHERE (cp.title LIKE ? OR cp.content LIKE ?) ${extraWhere}
            GROUP BY cp.post_id
            ORDER BY ${orderBy}
            LIMIT ? OFFSET ?
        `, [search, search, limit, offset]);

        const [[{ total }]] = await db.query(`
            SELECT COUNT(DISTINCT cp.post_id) as total
            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            WHERE (cp.title LIKE ? OR cp.content LIKE ?) ${extraWhere}
        `, [search, search]);

        return ok(res, { posts: rows, total, page, totalPages: Math.ceil(total / limit) });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// REMOVED: getErrorTypes - Community không cần error_types (thuộc Error Log System)

// POST /api/community/posts — đăng bài mới
const createPost = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { title, content, is_anonymous } = req.body;

        // Validate
        if (!title || title.trim().length < 10)
            return fail(res, 'Tiêu đề phải có ít nhất 10 ký tự', 400);
        if (title.trim().length > 200)
            return fail(res, 'Tiêu đề không được vượt quá 200 ký tự', 400);
        if (!content || title.trim().length < 20)
            return fail(res, 'Nội dung phải có ít nhất 20 ký tự', 400);
        if (content.trim().length > 2000)
            return fail(res, 'Nội dung không được vượt quá 2000 ký tự', 400);

        // Lấy student_id từ user_id
        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy thông tin sinh viên', 404);

        const [result] = await db.query(`
            INSERT INTO community_posts (student_id, title, content, is_anonymous, created_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
        `, [student.student_id, title.trim(), content.trim(), is_anonymous ? 1 : 0]);

        const postId = result.insertId;

        return ok(res, { post_id: postId },
            'Bài viết đã được đăng thành công!', 201);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/posts/:postId/vote
const votePost = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { postId } = req.params;
        const { vote_type } = req.body; // UPVOTE | DOWNVOTE

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        // Kiểm tra đã vote chưa
        const [[existing]] = await db.query(
            'SELECT vote_id FROM post_votes WHERE post_id = ? AND student_id = ?',
            [postId, student.student_id]
        );

        if (existing) {
            // Cập nhật vote
            await db.query('UPDATE post_votes SET vote_type = ? WHERE vote_id = ?',
                [vote_type, existing.vote_id]);
        } else {
            await db.query('INSERT INTO post_votes (post_id, student_id, vote_type) VALUES (?, ?, ?)',
                [postId, student.student_id, vote_type]);
        }

        return ok(res, null, 'Vote thành công');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// REMOVED: acceptQuestSuggestion - Thuộc Error Log System, không phải Community
// const acceptQuestSuggestion = async (req, res) => {
//     try {
//         const userId = req.user.user_id;
//         const { quest_id, error_log_id } = req.body;

//         const [[student]] = await db.query(
//             'SELECT student_id FROM students WHERE user_id = ?', [userId]
//         );
//         if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

//         await db.query(`
//             INSERT INTO user_quests (student_id, quest_id, error_log_id, status, assigned_at)
//             VALUES (?, ?, ?, 'pending', CURRENT_TIMESTAMP)
//         `, [student.student_id, quest_id, error_log_id || null]);

//         return ok(res, null, 'Đã nhận Quest thành công');
//     } catch (err) {
//         return fail(res, 'Server error', 500, err.message);
//     }
// };

const getPostDetail = async (req, res) => {
    try {
        const { postId } = req.params;

        // Tăng view_count
        await db.query('UPDATE community_posts SET view_count = view_count + 1 WHERE post_id = ?', [postId]);

        const [[post]] = await db.query(`
            SELECT 
                cp.post_id, cp.title, cp.content, cp.is_anonymous, cp.created_at,
                cp.view_count,
                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.avatar_url END AS author_avatar,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'UPVOTE' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'DOWNVOTE' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count
            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            WHERE cp.post_id = ?
            GROUP BY cp.post_id
        `, [postId]);

        if (!post) return fail(res, 'Không tìm thấy bài viết', 404);

        // Lấy comments kèm vote count (chỉ lấy top-level comments)
        const [comments] = await db.query(`
            SELECT 
                c.comment_id, c.content, c.created_at, c.view_count, c.parent_comment_id,
                CASE WHEN c.is_anonymous = 1 THEN 'Ẩn danh' ELSE COALESCE(u.name, 'Người dùng') END AS author_name,
                u.avatar_url AS author_avatar,
                COUNT(DISTINCT CASE WHEN cv.vote_type = 'UPVOTE' THEN cv.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN cv.vote_type = 'DOWNVOTE' THEN cv.vote_id END) AS downvote_count
            FROM comments c
            LEFT JOIN students s ON c.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN comment_votes cv ON c.comment_id = cv.comment_id
            WHERE c.post_id = ? AND c.parent_comment_id IS NULL
            GROUP BY c.comment_id
            ORDER BY c.created_at ASC
        `, [postId]);

        // Lấy replies cho từng comment
        const [replies] = await db.query(`
            SELECT 
                c.comment_id, c.content, c.created_at, c.parent_comment_id,
                CASE WHEN c.is_anonymous = 1 THEN 'Ẩn danh' ELSE COALESCE(u.name, 'Người dùng') END AS author_name,
                COUNT(DISTINCT CASE WHEN cv.vote_type = 'UPVOTE' THEN cv.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN cv.vote_type = 'DOWNVOTE' THEN cv.vote_id END) AS downvote_count
            FROM comments c
            LEFT JOIN students s ON c.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN comment_votes cv ON c.comment_id = cv.comment_id
            WHERE c.post_id = ? AND c.parent_comment_id IS NOT NULL
            GROUP BY c.comment_id
            ORDER BY c.created_at ASC
        `, [postId]);

        // Gắn replies vào comment cha
        const replyMap = {};
        replies.forEach(r => {
            if (!replyMap[r.parent_comment_id]) replyMap[r.parent_comment_id] = [];
            replyMap[r.parent_comment_id].push(r);
        });
        comments.forEach(c => { c.replies = replyMap[c.comment_id] || []; });

        return ok(res, { post, comments });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/posts/:postId/comments — đăng bình luận
const createComment = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { postId } = req.params;
        const { content, parent_comment_id, is_anonymous } = req.body;
        const isAnon = is_anonymous === '1' || is_anonymous === true || is_anonymous === 1 ? 1 : 0;

        if (!content || content.trim().length < 1)
            return fail(res, 'Nội dung bình luận không được trống', 400);

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [result] = await db.query(
            'INSERT INTO comments (post_id, student_id, content, parent_comment_id, is_anonymous, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)',
            [postId, student.student_id, content.trim(), parent_comment_id || null, isAnon]
        );

        const [[comment]] = await db.query(`
            SELECT c.comment_id, c.content, c.created_at, c.parent_comment_id, c.is_anonymous,
                   CASE WHEN c.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                   u.avatar_url AS author_avatar,
                   0 AS upvote_count, 0 AS downvote_count
            FROM comments c
            LEFT JOIN students s ON c.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            WHERE c.comment_id = ?
        `, [result.insertId]);

        return ok(res, comment, 'Bình luận thành công', 201);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/posts/:postId/comments/:commentId/vote
const voteComment = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { commentId } = req.params;
        const { vote_type } = req.body;

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [[existing]] = await db.query(
            'SELECT vote_id FROM comment_votes WHERE comment_id = ? AND student_id = ?',
            [commentId, student.student_id]
        );

        if (existing) {
            await db.query('UPDATE COMMENT_VOTES SET vote_type = ? WHERE vote_id = ?',
                [vote_type, existing.vote_id]);
        } else {
            await db.query('INSERT INTO COMMENT_VOTES (comment_id, student_id, vote_type) VALUES (?, ?, ?)',
                [commentId, student.student_id, vote_type]);
        }

        return ok(res, null, 'Vote thành công');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/posts/:postId/save — lưu/bỏ lưu bài viết
const toggleSavePost = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { postId } = req.params;
        const [[student]] = await db.query('SELECT student_id FROM students WHERE user_id = ?', [userId]);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [[existing]] = await db.query(
            'SELECT id FROM saved_posts WHERE student_id = ? AND post_id = ?',
            [student.student_id, postId]
        );
        if (existing) {
            await db.query('DELETE FROM saved_posts WHERE id = ?', [existing.id]);
            return ok(res, { saved: false }, 'Đã bỏ lưu bài viết');
        } else {
            await db.query('INSERT INTO saved_posts (student_id, post_id) VALUES (?, ?)',
                [student.student_id, postId]);
            return ok(res, { saved: true }, 'Đã lưu bài viết');
        }
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/posts/:postId/mute — không quan tâm tác giả
const muteAuthor = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { postId } = req.params;
        const [[student]] = await db.query('SELECT student_id FROM students WHERE user_id = ?', [userId]);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        // Lấy student_id của tác giả bài viết
        const [[post]] = await db.query('SELECT student_id FROM community_posts WHERE post_id = ?', [postId]);
        if (!post) return fail(res, 'Không tìm thấy bài viết', 404);
        if (post.student_id === student.student_id) return fail(res, 'Không thể mute chính mình', 400);

        await db.query(
            'INSERT IGNORE INTO MUTED_AUTHORS (student_id, muted_student_id) VALUES (?, ?)',
            [student.student_id, post.student_id]
        );
        return ok(res, null, 'Đã ẩn bài viết từ tác giả này');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getSavedPosts = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const [[student]] = await db.query('SELECT student_id FROM students WHERE user_id = ?', [userId]);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [rows] = await db.query(`
            SELECT cp.post_id, cp.title, cp.content, cp.is_anonymous, cp.created_at,
                cp.view_count,
                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'UPVOTE' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'DOWNVOTE' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count
            FROM saved_posts sp
            JOIN community_posts cp ON sp.post_id = cp.post_id
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            WHERE sp.student_id = ?
            GROUP BY cp.post_id
            ORDER BY sp.saved_at DESC
        `, [student.student_id]);

        return ok(res, { posts: rows, total: rows.length });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

module.exports = {
    getPosts, createPost, votePost,
    getPostDetail, createComment, voteComment,
    toggleSavePost, muteAuthor, getSavedPosts
};
