const db = require('../config/db');
const { ok, fail } = require('../utils/response');

// GET /api/community/posts?filter=new&page=1
const getPosts = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = 20;
        const offset = (page - 1) * limit;
        const filter = req.query.filter || 'new'; // new | trending | best | unfixed | my_logs
        const search = req.query.search ? `%${req.query.search}%` : '%';
        const errorTypeId = req.query.error_type_id ? parseInt(req.query.error_type_id) : null;
        const userId = req.user.user_id;

        let orderBy = 'cp.created_at DESC';
        let extraWhere = errorTypeId ? `AND cp.error_type_id = ${errorTypeId}` : '';

        if (filter === 'trending') {
            orderBy = 'upvote_count DESC, cp.created_at DESC';
        } else if (filter === 'best') {
            orderBy = 'comment_count DESC, upvote_count DESC';
        } else if (filter === 'unfixed') {
            extraWhere += ` AND NOT EXISTS (
                SELECT 1 FROM USERQUESTS uq
                WHERE uq.error_log_id IN (
                    SELECT el.error_log_id FROM ERRORLOGS el
                    JOIN STUDENTS st ON el.student_id = st.student_id
                    WHERE st.user_id = s.user_id
                ) AND uq.status = 'completed'
            )`;
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
                cp.error_type_id,
                et.error_name,
                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.avatar_url END AS author_avatar,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'UPVOTE_FIX' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type != 'UPVOTE_FIX' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                EXISTS(SELECT 1 FROM SAVED_POSTS sp2 WHERE sp2.post_id = cp.post_id AND sp2.student_id = (SELECT student_id FROM STUDENTS WHERE user_id = ${userId} LIMIT 1)) AS is_saved
            FROM COMMUNITYPOSTS cp
            LEFT JOIN STUDENTS s ON cp.student_id = s.student_id
            LEFT JOIN USERS u ON s.user_id = u.user_id
            LEFT JOIN ERRORTYPES et ON cp.error_type_id = et.error_type_id
            LEFT JOIN VOTES v ON cp.post_id = v.post_id
            LEFT JOIN COMMENTS c ON cp.post_id = c.post_id
            WHERE (cp.title LIKE ? OR cp.content LIKE ?) ${extraWhere}
            GROUP BY cp.post_id
            ORDER BY ${orderBy}
            LIMIT ? OFFSET ?
        `, [search, search, limit, offset]);

        const [[{ total }]] = await db.query(`
            SELECT COUNT(DISTINCT cp.post_id) as total
            FROM COMMUNITYPOSTS cp
            LEFT JOIN STUDENTS s ON cp.student_id = s.student_id
            WHERE (cp.title LIKE ? OR cp.content LIKE ?) ${extraWhere}
        `, [search, search]);

        return ok(res, { posts: rows, total, page, totalPages: Math.ceil(total / limit) });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// GET /api/community/error-types — lấy danh sách loại vấn đề để chọn khi đăng bài
const getErrorTypes = async (req, res) => {
    try {
        const [rows] = await db.query('SELECT error_type_id, error_name FROM ERRORTYPES ORDER BY error_name');
        return ok(res, rows);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/posts — đăng bài mới
const createPost = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { title, content, error_type_id, is_anonymous } = req.body;

        // Validate
        if (!title || title.trim().length < 10)
            return fail(res, 'Tiêu đề phải có ít nhất 10 ký tự', 400);
        if (title.trim().length > 200)
            return fail(res, 'Tiêu đề không được vượt quá 200 ký tự', 400);
        if (!content || content.trim().length < 20)
            return fail(res, 'Nội dung phải có ít nhất 20 ký tự', 400);
        if (content.trim().length > 2000)
            return fail(res, 'Nội dung không được vượt quá 2000 ký tự', 400);
        if (!error_type_id)
            return fail(res, 'Vui lòng chọn loại vấn đề', 400);

        // Lấy student_id từ user_id
        const [[student]] = await db.query(
            'SELECT student_id FROM STUDENTS WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy thông tin sinh viên', 404);

        const [result] = await db.query(`
            INSERT INTO COMMUNITYPOSTS (student_id, error_type_id, title, content, is_anonymous, created_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        `, [student.student_id, error_type_id, title.trim(), content.trim(), is_anonymous ? 1 : 0]);

        const postId = result.insertId;

        // Kiểm tra gợi ý Quest (bước 13 trong flow)
        const [[recentLog]] = await db.query(`
            SELECT el.error_log_id, el.error_type_id, et.error_name
            FROM ERRORLOGS el
            JOIN ERRORTYPES et ON el.error_type_id = et.error_type_id
            WHERE el.student_id = ? AND el.error_type_id = ?
            ORDER BY el.created_at DESC
            LIMIT 1
        `, [student.student_id, error_type_id]);

        let questSuggestion = null;
        if (recentLog) {
            const [[quest]] = await db.query(`
                SELECT q.quest_id, q.quest_title
                FROM QUESTS q
                WHERE q.error_type_id = ?
                AND q.quest_id NOT IN (
                    SELECT uq.quest_id FROM USERQUESTS uq WHERE uq.student_id = ?
                )
                LIMIT 1
            `, [error_type_id, student.student_id]);

            if (quest) {
                questSuggestion = {
                    quest_id: quest.quest_id,
                    quest_title: quest.quest_title,
                    error_name: recentLog.error_name
                };
            }
        }

        return ok(res, { post_id: postId, quest_suggestion: questSuggestion },
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
        const { vote_type } = req.body; // UPVOTE_FIX | REPRODUCE_ERROR

        const [[student]] = await db.query(
            'SELECT student_id FROM STUDENTS WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        // Kiểm tra đã vote chưa
        const [[existing]] = await db.query(
            'SELECT vote_id FROM VOTES WHERE post_id = ? AND student_id = ?',
            [postId, student.student_id]
        );

        if (existing) {
            // Cập nhật vote
            await db.query('UPDATE VOTES SET vote_type = ? WHERE vote_id = ?',
                [vote_type, existing.vote_id]);
        } else {
            await db.query('INSERT INTO VOTES (post_id, student_id, vote_type) VALUES (?, ?, ?)',
                [postId, student.student_id, vote_type]);
        }

        return ok(res, null, 'Vote thành công');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/quests/accept — nhận quest gợi ý
const acceptQuestSuggestion = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { quest_id, error_log_id } = req.body;

        const [[student]] = await db.query(
            'SELECT student_id FROM STUDENTS WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        await db.query(`
            INSERT INTO USERQUESTS (student_id, quest_id, error_log_id, status, assigned_at)
            VALUES (?, ?, ?, 'pending', CURRENT_TIMESTAMP)
        `, [student.student_id, quest_id, error_log_id || null]);

        return ok(res, null, 'Đã nhận Quest thành công');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getPostDetail = async (req, res) => {
    try {
        const { postId } = req.params;

        // Tăng view_count
        await db.query('UPDATE COMMUNITYPOSTS SET view_count = view_count + 1 WHERE post_id = ?', [postId]);

        const [[post]] = await db.query(`
            SELECT 
                cp.post_id, cp.title, cp.content, cp.is_anonymous, cp.created_at,
                cp.view_count, cp.error_type_id, et.error_name,
                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.avatar_url END AS author_avatar,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'UPVOTE_FIX' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type != 'UPVOTE_FIX' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count
            FROM COMMUNITYPOSTS cp
            LEFT JOIN STUDENTS s ON cp.student_id = s.student_id
            LEFT JOIN USERS u ON s.user_id = u.user_id
            LEFT JOIN ERRORTYPES et ON cp.error_type_id = et.error_type_id
            LEFT JOIN VOTES v ON cp.post_id = v.post_id
            LEFT JOIN COMMENTS c ON cp.post_id = c.post_id
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
                COUNT(DISTINCT CASE WHEN cv.vote_type = 'UPVOTE_FIX' THEN cv.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN cv.vote_type != 'UPVOTE_FIX' THEN cv.vote_id END) AS downvote_count
            FROM COMMENTS c
            LEFT JOIN STUDENTS s ON c.student_id = s.student_id
            LEFT JOIN USERS u ON s.user_id = u.user_id
            LEFT JOIN COMMENT_VOTES cv ON c.comment_id = cv.comment_id
            WHERE c.post_id = ? AND c.parent_comment_id IS NULL
            GROUP BY c.comment_id
            ORDER BY c.created_at ASC
        `, [postId]);

        // Lấy replies cho từng comment
        const [replies] = await db.query(`
            SELECT 
                c.comment_id, c.content, c.created_at, c.parent_comment_id,
                CASE WHEN c.is_anonymous = 1 THEN 'Ẩn danh' ELSE COALESCE(u.name, 'Người dùng') END AS author_name,
                COUNT(DISTINCT CASE WHEN cv.vote_type = 'UPVOTE_FIX' THEN cv.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN cv.vote_type != 'UPVOTE_FIX' THEN cv.vote_id END) AS downvote_count
            FROM COMMENTS c
            LEFT JOIN STUDENTS s ON c.student_id = s.student_id
            LEFT JOIN USERS u ON s.user_id = u.user_id
            LEFT JOIN COMMENT_VOTES cv ON c.comment_id = cv.comment_id
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
            'SELECT student_id FROM STUDENTS WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [result] = await db.query(
            'INSERT INTO COMMENTS (post_id, student_id, content, parent_comment_id, is_anonymous, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)',
            [postId, student.student_id, content.trim(), parent_comment_id || null, isAnon]
        );

        const [[comment]] = await db.query(`
            SELECT c.comment_id, c.content, c.created_at, c.parent_comment_id, c.is_anonymous,
                   CASE WHEN c.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                   u.avatar_url AS author_avatar,
                   0 AS upvote_count, 0 AS downvote_count
            FROM COMMENTS c
            LEFT JOIN STUDENTS s ON c.student_id = s.student_id
            LEFT JOIN USERS u ON s.user_id = u.user_id
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
            'SELECT student_id FROM STUDENTS WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [[existing]] = await db.query(
            'SELECT vote_id FROM COMMENT_VOTES WHERE comment_id = ? AND student_id = ?',
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
        const [[student]] = await db.query('SELECT student_id FROM STUDENTS WHERE user_id = ?', [userId]);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [[existing]] = await db.query(
            'SELECT id FROM SAVED_POSTS WHERE student_id = ? AND post_id = ?',
            [student.student_id, postId]
        );
        if (existing) {
            await db.query('DELETE FROM SAVED_POSTS WHERE id = ?', [existing.id]);
            return ok(res, { saved: false }, 'Đã bỏ lưu bài viết');
        } else {
            await db.query('INSERT INTO SAVED_POSTS (student_id, post_id) VALUES (?, ?)',
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
        const [[student]] = await db.query('SELECT student_id FROM STUDENTS WHERE user_id = ?', [userId]);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        // Lấy student_id của tác giả bài viết
        const [[post]] = await db.query('SELECT student_id FROM COMMUNITYPOSTS WHERE post_id = ?', [postId]);
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

// GET /api/community/saved — lấy danh sách bài viết đã lưu
const getSavedPosts = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const [[student]] = await db.query('SELECT student_id FROM STUDENTS WHERE user_id = ?', [userId]);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [rows] = await db.query(`
            SELECT cp.post_id, cp.title, cp.content, cp.is_anonymous, cp.created_at,
                cp.view_count, cp.error_type_id, et.error_name,
                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'UPVOTE_FIX' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type != 'UPVOTE_FIX' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count
            FROM SAVED_POSTS sp
            JOIN COMMUNITYPOSTS cp ON sp.post_id = cp.post_id
            LEFT JOIN STUDENTS s ON cp.student_id = s.student_id
            LEFT JOIN USERS u ON s.user_id = u.user_id
            LEFT JOIN ERRORTYPES et ON cp.error_type_id = et.error_type_id
            LEFT JOIN VOTES v ON cp.post_id = v.post_id
            LEFT JOIN COMMENTS c ON cp.post_id = c.post_id
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
    getPosts, getErrorTypes, createPost, votePost, acceptQuestSuggestion,
    getPostDetail, createComment, voteComment,
    toggleSavePost, muteAuthor, getSavedPosts
};
