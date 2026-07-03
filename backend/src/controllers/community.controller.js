const db = require('../config/db');
const { ok, fail } = require('../utils/response');

// GET /api/community/profile/me
const getMyCommunityProfile = async (req, res) => {
    try {
        const userId = req.user.user_id;

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
            [userId]
        );

        if (!student) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        const [[profile]] = await db.query(`
            SELECT 
                cp.profile_id,
                cp.student_id,
                cp.username,
                cp.display_name,
                COALESCE(cp.avatar_url, u.avatar_url) AS avatar_url,
                cp.cover_url,
                cp.bio,
                cp.music_url,
                cp.music_name,
                cp.follower_count,
                cp.following_count,
                (
                    SELECT COUNT(*) FROM community_posts p
                    WHERE p.student_id = cp.student_id
                      AND p.is_anonymous = 0
                      AND p.is_hidden = 0
                ) AS post_count,
                s.major,
                s.faculty,
                s.year_of_study,
                TRUE AS is_me,
                FALSE AS followed_by_me
            FROM community_profiles cp
            JOIN students s ON cp.student_id = s.student_id
            JOIN users u ON s.user_id = u.user_id
            WHERE cp.student_id = ?
            LIMIT 1
        `, [student.student_id]);

        if (!profile) {
            return fail(res, 'Chưa có profile cộng đồng', 404);
        }

        profile.is_me = Boolean(profile.is_me);
        profile.followed_by_me = Boolean(profile.followed_by_me);

        return ok(res, profile);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// GET /api/community/profile/:studentId
const getCommunityProfile = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { studentId } = req.params;

        const currentStudent = await getCurrentStudent(userId);
        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        const [[profile]] = await db.query(`
            SELECT 
                cp.profile_id,
                cp.student_id,
                cp.username,
                cp.display_name,
                COALESCE(cp.avatar_url, u.avatar_url) AS avatar_url,
                cp.cover_url,
                cp.bio,
                cp.music_url,
                cp.music_name,
                cp.follower_count,
                cp.following_count,
                (
                    SELECT COUNT(*) FROM community_posts p
                    WHERE p.student_id = cp.student_id
                      AND p.is_anonymous = 0
                      AND p.is_hidden = 0
                ) AS post_count,
                s.major,
                s.faculty,
                s.year_of_study,
                CASE WHEN cp.student_id = ? THEN TRUE ELSE FALSE END AS is_me,
                EXISTS (
                    SELECT 1
                    FROM community_follows cf
                    WHERE cf.follower_student_id = ?
                      AND cf.following_student_id = cp.student_id
                ) AS followed_by_me
            FROM community_profiles cp
            JOIN students s ON cp.student_id = s.student_id
            JOIN users u ON s.user_id = u.user_id
            WHERE cp.student_id = ?
            LIMIT 1
        `, [currentStudent.student_id, currentStudent.student_id, studentId]);

        if (!profile) {
            return fail(res, 'Không tìm thấy profile cộng đồng', 404);
        }

        profile.is_me = Boolean(profile.is_me);
        profile.followed_by_me = Boolean(profile.followed_by_me);

        return ok(res, profile);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// PUT /api/community/profile/me
const updateMyCommunityProfile = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const {
            username,
            display_name,
            avatar_url,
            cover_url,
            bio,
            music_url,
            music_name
        } = req.body;

        const student = await getCurrentStudent(userId);
        if (!student) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        const [[existing]] = await db.query(
            'SELECT username, display_name, avatar_url, cover_url, bio, music_url, music_name FROM community_profiles WHERE student_id = ? LIMIT 1',
            [student.student_id]
        );
        if (!existing) {
            return fail(res, 'Chưa có profile cộng đồng', 404);
        }

        // Edit-profile rút gọn có thể không gửi username/display_name → giữ giá trị cũ.
        const finalUsername = (username !== undefined && username !== null && username.trim() !== '')
            ? username.trim()
            : existing.username;
        const finalDisplayName = (display_name !== undefined && display_name !== null && display_name.trim() !== '')
            ? display_name.trim()
            : existing.display_name;

        if (!finalUsername || finalUsername.length < 3) {
            return fail(res, 'Username phải có ít nhất 3 ký tự', 400);
        }
        if (!finalDisplayName || finalDisplayName.length < 2) {
            return fail(res, 'Tên hiển thị không hợp lệ', 400);
        }

        await db.query(`
            UPDATE community_profiles
            SET
                username = ?,
                display_name = ?,
                avatar_url = ?,
                cover_url = ?,
                bio = ?,
                music_url = ?,
                music_name = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE student_id = ?
        `, [
            finalUsername,
            finalDisplayName,
            avatar_url !== undefined ? (avatar_url || null) : existing.avatar_url,
            cover_url !== undefined ? (cover_url || null) : existing.cover_url,
            bio !== undefined ? (bio || null) : existing.bio,
            music_url !== undefined ? (music_url || null) : existing.music_url,
            music_name !== undefined ? (music_name || null) : existing.music_name,
            student.student_id
        ]);

        return ok(res, null, 'Cập nhật profile thành công');
    } catch (err) {
        if (err.code === 'ER_DUP_ENTRY') {
            return fail(res, 'Username đã tồn tại', 409);
        }

        return fail(res, 'Server error', 500, err.message);
    }
};

// GET /api/community/profile/:studentId/posts
const getCommunityProfilePosts = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { studentId } = req.params;

        const viewer = await getCurrentStudent(userId);
        const myStudentId = viewer ? viewer.student_id : 0;

        const [rows] = await db.query(`
            SELECT
                cp.post_id,
                cp.student_id,
                cp.title,
                cp.content,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,
                cp.topic_id,
                pt.topic_name AS topic_name,

                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE COALESCE(cprof.avatar_url, u.avatar_url) END AS author_avatar,

                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                COUNT(DISTINCT rp.repost_id) AS repost_count,

                EXISTS(
                    SELECT 1
                    FROM saved_posts sp2
                    WHERE sp2.post_id = cp.post_id
                    AND sp2.student_id = ${myStudentId}
                ) AS is_saved,

                EXISTS(
                    SELECT 1
                    FROM post_reposts rpx
                    WHERE rpx.post_id = cp.post_id
                    AND rpx.student_id = ${myStudentId}
                ) AS is_reposted

            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN community_profiles cprof ON cp.student_id = cprof.student_id
            LEFT JOIN post_topics pt ON cp.topic_id = pt.topic_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id
            WHERE cp.student_id = ?
              AND cp.is_anonymous = 0
              AND cp.is_hidden = 0
            GROUP BY cp.post_id
            ORDER BY cp.created_at DESC
        `, [studentId]);

        return ok(res, { posts: rows, total: rows.length });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getCommunityProfileReplies = async (req, res) => {
    try {
        const { studentId } = req.params;

        const [rows] = await db.query(`
            SELECT 
                c.comment_id,
                c.post_id,
                c.student_id,
                CONCAT('Trả lời bài viết: ', cp.title) AS title,
                c.content,
                c.is_anonymous,
                c.created_at,
                0 AS view_count,

                CASE WHEN c.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN c.is_anonymous = 1 THEN NULL ELSE u.avatar_url END AS author_avatar,

                COUNT(DISTINCT CASE WHEN cv.vote_type = 'upvote' THEN cv.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN cv.vote_type = 'downvote' THEN cv.vote_id END) AS downvote_count,
                0 AS comment_count,
                0 AS is_saved

            FROM comments c
            JOIN community_posts cp ON c.post_id = cp.post_id
            LEFT JOIN students s ON c.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN comment_votes cv ON c.comment_id = cv.comment_id
            WHERE c.student_id = ?
              AND c.is_anonymous = 0
            GROUP BY c.comment_id
            ORDER BY c.created_at DESC
        `, [studentId]);

        return ok(res, { posts: rows, total: rows.length });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getCommunityProfileMedia = async (req, res) => {
    try {
        return ok(res, { posts: [], total: 0 });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getCommunityProfileReposts = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { studentId } = req.params;

        const [rows] = await db.query(`
            SELECT
                cp.post_id,
                cp.student_id,
                cp.title,
                cp.content,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,
                cp.topic_id,
                pt.topic_name,

                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE COALESCE(cprof.avatar_url, u.avatar_url) END AS author_avatar,

                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                COUNT(DISTINCT rp2.repost_id) AS repost_count,

                EXISTS(
                    SELECT 1
                    FROM post_reposts rpme
                    WHERE rpme.post_id = cp.post_id
                    AND rpme.student_id = (
                        SELECT student_id FROM students WHERE user_id = ? LIMIT 1
                    )
                ) AS is_reposted

            FROM post_reposts rp
            JOIN community_posts cp ON rp.post_id = cp.post_id
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN community_profiles cprof ON cp.student_id = cprof.student_id
            LEFT JOIN post_topics pt ON cp.topic_id = pt.topic_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp2 ON cp.post_id = rp2.post_id
            WHERE rp.student_id = ?
              AND cp.is_hidden = 0
            GROUP BY cp.post_id
            ORDER BY rp.created_at DESC
        `, [userId, studentId]);

        return ok(res, { posts: rows, total: rows.length });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// GET /api/community/profile/me/saved — bài viết đã lưu (chỉ của chính mình)
const getCommunityProfileSaved = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const [[student]] = await db.query('SELECT student_id FROM students WHERE user_id = ? LIMIT 1', [userId]);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [rows] = await db.query(`
            SELECT
                cp.post_id,
                cp.student_id,
                cp.title,
                cp.content,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,
                cp.topic_id,
                pt.topic_name,

                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE COALESCE(cprof.avatar_url, u.avatar_url) END AS author_avatar,

                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                COUNT(DISTINCT rp.repost_id) AS repost_count,
                1 AS is_saved

            FROM saved_posts sp
            JOIN community_posts cp ON sp.post_id = cp.post_id
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN community_profiles cprof ON cp.student_id = cprof.student_id
            LEFT JOIN post_topics pt ON cp.topic_id = pt.topic_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id
            WHERE sp.student_id = ?
              AND cp.is_hidden = 0
            GROUP BY cp.post_id
            ORDER BY sp.saved_at DESC
        `, [student.student_id]);

        return ok(res, { posts: rows, total: rows.length });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};


// POST /api/community/users/:studentId/follow
const followUser = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { studentId } = req.params;

        const currentStudent = await getCurrentStudent(userId);
        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        if (parseInt(studentId) === currentStudent.student_id) {
            return fail(res, 'Không thể theo dõi chính mình', 400);
        }

        const [[target]] = await db.query(
            'SELECT student_id FROM students WHERE student_id = ? LIMIT 1',
            [studentId]
        );

        if (!target) {
            return fail(res, 'Không tìm thấy người dùng cần theo dõi', 404);
        }

        // Trigger trg_follows_after_insert tự đồng bộ follower_count/following_count.
        await db.query(`
            INSERT IGNORE INTO community_follows
            (follower_student_id, following_student_id)
            VALUES (?, ?)
        `, [currentStudent.student_id, studentId]);

        return ok(res, { followed: true }, 'Đã theo dõi');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// DELETE /api/community/users/:studentId/follow
const unfollowUser = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { studentId } = req.params;

        const currentStudent = await getCurrentStudent(userId);
        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        // Trigger trg_follows_after_delete tự đồng bộ follower_count/following_count.
        await db.query(`
            DELETE FROM community_follows
            WHERE follower_student_id = ?
              AND following_student_id = ?
        `, [currentStudent.student_id, studentId]);

        return ok(res, { followed: false }, 'Đã bỏ theo dõi');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// GET /api/community/topics
const getPostTopics = async (req, res) => {
    try {
        const [topics] = await db.query(`
            SELECT 
                topic_id,
                topic_name,
                topic_description,
                icon_url,
                color_hex
            FROM post_topics
            ORDER BY topic_name ASC
        `);

        return ok(res, { topics });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};


// GET /api/community/posts?filter=new&page=1
const getPosts = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = 20;
        const offset = (page - 1) * limit;
        const filter = req.query.filter || 'new'; // new | trending | best | topic
        const rawSearch = req.query.search ? req.query.search.trim() : '';
        const hasSearch = rawSearch.length > 0;
        const search = hasSearch ? `%${rawSearch}%` : '%';
        const topicId = req.query.topic_id ? parseInt(req.query.topic_id) : null;
        const userId = req.user.user_id;

        const [[me]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
            [userId]
        );
        const myStudentId = me ? me.student_id : 0;

        let orderBy = 'cp.created_at DESC';
        let extraWhere = '';

        if (filter === 'trending') {
            // Xếp theo tổng tương tác (upvote + bình luận + repost + lượt xem), mới hơn ưu tiên khi hoà.
            // Không giới hạn cửa sổ thời gian để feed không bị rỗng khi dữ liệu cũ.
            orderBy = '(COUNT(DISTINCT CASE WHEN v.vote_type = \'upvote\' THEN v.vote_id END) + COUNT(DISTINCT c.comment_id) + COUNT(DISTINCT rp.repost_id) + cp.view_count) DESC, cp.created_at DESC';
        } else if (filter === 'best') {
            orderBy = 'upvote_count DESC, cp.created_at DESC';
        } else if (filter === 'topic' && topicId) {
            extraWhere += ` AND cp.topic_id = ${topicId}`;
        }

        // Luôn ẩn bài đã bị admin ẩn và bài của tác giả bị block.
        // Mute chỉ áp dụng cho feed (không áp khi user chủ động tìm kiếm).
        extraWhere += ' AND cp.is_hidden = 0';
        extraWhere += ` AND cp.student_id NOT IN (SELECT blocked_student_id FROM blocked_authors WHERE student_id = ${myStudentId})`;
        if (!hasSearch) {
            extraWhere += ` AND cp.student_id NOT IN (SELECT muted_student_id FROM MUTED_AUTHORS WHERE student_id = ${myStudentId})`;
        }

        const [rows] = await db.query(`
            SELECT
                cp.post_id,
                cp.student_id,
                cp.title,
                cp.content,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,
                cp.topic_id,
                pt.topic_name,

                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE COALESCE(cprof.avatar_url, u.avatar_url) END AS author_avatar,

                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,

                COUNT(DISTINCT rp.repost_id) AS repost_count,

                EXISTS(
                    SELECT 1
                    FROM saved_posts sp2
                    WHERE sp2.post_id = cp.post_id
                    AND sp2.student_id = ${myStudentId}
                ) AS is_saved,

                EXISTS(
                    SELECT 1
                    FROM post_reposts rp2
                    WHERE rp2.post_id = cp.post_id
                    AND rp2.student_id = ${myStudentId}
                ) AS is_reposted

            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN community_profiles cprof ON cp.student_id = cprof.student_id
            LEFT JOIN post_topics pt ON cp.topic_id = pt.topic_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id

            WHERE (cp.title LIKE ? OR cp.content LIKE ? OR u.name LIKE ?) ${extraWhere}
            GROUP BY cp.post_id
            ORDER BY ${orderBy}
            LIMIT ? OFFSET ?
        `, [search, search, search, limit, offset]);

        const [[{ total }]] = await db.query(`
            SELECT COUNT(DISTINCT cp.post_id) as total
            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            WHERE (cp.title LIKE ? OR cp.content LIKE ? OR u.name LIKE ?) ${extraWhere}
        `, [search, search, search]);

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
        const { title, content, is_anonymous, topic_id } = req.body;

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

        const topicId = topic_id ? parseInt(topic_id) : null;

        const [result] = await db.query(`
            INSERT INTO community_posts (student_id, title, content, is_anonymous, topic_id, created_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        `, [student.student_id, title.trim(), content.trim(), is_anonymous ? 1 : 0, topicId]);

        const postId = result.insertId;

        return ok(res, { post_id: postId },
            'Bài viết đã được đăng thành công!', 201);
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/profile/me/music — upload file mp3, trả về đường dẫn
const uploadProfileMusic = async (req, res) => {
    try {
        if (!req.file) {
            return fail(res, 'Chưa có file nhạc', 400);
        }
        const musicUrl = `/uploads/music/${req.file.filename}`;
        return ok(res, { music_url: musicUrl }, 'Tải nhạc lên thành công');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// PUT /api/community/posts/:postId — sửa bài (chỉ tác giả, không đổi ẩn danh)
const updatePost = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { postId } = req.params;
        const { title, content, topic_id } = req.body;

        if (!title || title.trim().length < 10)
            return fail(res, 'Tiêu đề phải có ít nhất 10 ký tự', 400);
        if (title.trim().length > 200)
            return fail(res, 'Tiêu đề không được vượt quá 200 ký tự', 400);
        if (!content || content.trim().length < 20)
            return fail(res, 'Nội dung phải có ít nhất 20 ký tự', 400);
        if (content.trim().length > 2000)
            return fail(res, 'Nội dung không được vượt quá 2000 ký tự', 400);

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ?', [userId]
        );
        if (!student) return fail(res, 'Không tìm thấy thông tin sinh viên', 404);

        const [[post]] = await db.query(
            'SELECT student_id FROM community_posts WHERE post_id = ?', [postId]
        );
        if (!post) return fail(res, 'Không tìm thấy bài viết', 404);
        if (post.student_id !== student.student_id)
            return fail(res, 'Chỉ tác giả mới có thể chỉnh sửa bài viết', 403);

        const topicId = topic_id ? parseInt(topic_id) : null;

        await db.query(`
            UPDATE community_posts
            SET title = ?, content = ?, topic_id = ?
            WHERE post_id = ?
        `, [title.trim(), content.trim(), topicId, postId]);

        return ok(res, { post_id: parseInt(postId) }, 'Cập nhật bài viết thành công');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// DELETE /api/community/posts/:postId — xóa bài (chỉ tác giả)
const deletePost = async (req, res) => {
    const conn = await db.getConnection();
    try {
        const userId = req.user.user_id;
        const { postId } = req.params;

        const [[student]] = await conn.query(
            'SELECT student_id FROM students WHERE user_id = ?', [userId]
        );
        if (!student) {
            conn.release();
            return fail(res, 'Không tìm thấy thông tin sinh viên', 404);
        }

        const [[post]] = await conn.query(
            'SELECT student_id FROM community_posts WHERE post_id = ?', [postId]
        );
        if (!post) {
            conn.release();
            return fail(res, 'Không tìm thấy bài viết', 404);
        }
        if (post.student_id !== student.student_id) {
            conn.release();
            return fail(res, 'Chỉ tác giả mới có thể xóa bài viết', 403);
        }

        // Mọi bảng con (post_votes, comments+comment_votes, saved_posts,
        // post_reposts, post_reports, post_review_requests, post_topic_mapping)
        // đều ON DELETE CASCADE; community_notifications SET NULL. Chỉ cần xóa post.
        await conn.beginTransaction();
        await conn.query('DELETE FROM community_posts WHERE post_id = ?', [postId]);
        await conn.commit();

        return ok(res, null, 'Đã xóa bài viết');
    } catch (err) {
        await conn.rollback();
        return fail(res, 'Server error', 500, err.message);
    } finally {
        conn.release();
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
        const userId = req.user.user_id;

        // Tăng view_count
        await db.query('UPDATE community_posts SET view_count = view_count + 1 WHERE post_id = ?', [postId]);

        // Xem nội dung bài viết = minh chứng quan tâm lại tác giả → tự gỡ mute.
        const [[viewer]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
            [userId]
        );
        if (viewer) {
            const [[postAuthor]] = await db.query(
                'SELECT student_id FROM community_posts WHERE post_id = ? LIMIT 1',
                [postId]
            );
            if (postAuthor) {
                await db.query(
                    'DELETE FROM MUTED_AUTHORS WHERE student_id = ? AND muted_student_id = ?',
                    [viewer.student_id, postAuthor.student_id]
                );
            }
        }

        const [[post]] = await db.query(`
            SELECT 
                cp.post_id, cp.title, cp.content, cp.is_anonymous, cp.created_at,
                cp.view_count,
                cp.student_id,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.email END AS author_email,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE s.student_code END AS author_student_code,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE s.major END AS author_major,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE s.faculty END AS author_faculty,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE s.year_of_study END AS author_year_of_study,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
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

const toggleRepostPost = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { postId } = req.params;

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
            [userId]
        );

        if (!student) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        const [[post]] = await db.query(
            'SELECT post_id FROM community_posts WHERE post_id = ? LIMIT 1',
            [postId]
        );

        if (!post) {
            return fail(res, 'Không tìm thấy bài viết', 404);
        }

        const [[existing]] = await db.query(
            'SELECT repost_id FROM post_reposts WHERE post_id = ? AND student_id = ? LIMIT 1',
            [postId, student.student_id]
        );

        if (existing) {
            await db.query(
                'DELETE FROM post_reposts WHERE repost_id = ?',
                [existing.repost_id]
            );

            return ok(res, { reposted: false }, 'Đã bỏ đăng lại');
        }

        await db.query(
            'INSERT INTO post_reposts (post_id, student_id) VALUES (?, ?)',
            [postId, student.student_id]
        );

        return ok(res, { reposted: true }, 'Đã đăng lại bài viết');
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
            SELECT
                cp.post_id,
                cp.student_id,
                cp.title,
                cp.content,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,
                cp.topic_id,
                pt.topic_name,
                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.avatar_url END AS author_avatar,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.email END AS author_email,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE s.student_code END AS author_student_code,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE s.major END AS author_major,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE s.faculty END AS author_faculty,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE s.year_of_study END AS author_year_of_study,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                COUNT(DISTINCT rp.repost_id) AS repost_count,
                1 AS is_saved,

                EXISTS(
                    SELECT 1
                    FROM post_reposts rpme
                    WHERE rpme.post_id = cp.post_id
                    AND rpme.student_id = ?
                ) AS is_reposted
            FROM saved_posts sp
            JOIN community_posts cp ON sp.post_id = cp.post_id
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN post_topics pt ON cp.topic_id = pt.topic_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id
            WHERE sp.student_id = ?
            GROUP BY cp.post_id
            ORDER BY sp.saved_at DESC
        `, [student.student_id, student.student_id]);

        return ok(res, { posts: rows, total: rows.length });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getCurrentStudent = async (userId) => {
    const [[student]] = await db.query(
        'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
        [userId]
    );
    return student;
};

const getUserIdFromReq = (req) => {
    return (
        req.user?.user_id ||
        req.user?.userId ||
        req.user?.id ||
        req.user?.user?.user_id
    );
};

const getProfileFollowers = async (req, res) => {
    try {
        const userId = getUserIdFromReq(req);
        const { studentId } = req.params;

        const [[viewer]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
            [userId]
        );

        if (!viewer) {
            return fail(res, 'Không tìm thấy sinh viên hiện tại', 404);
        }

        const viewerStudentId = viewer.student_id;

        const [users] = await db.query(`
            SELECT
                cp.student_id,
                cp.username,
                cp.display_name,
                cp.avatar_url,
                cp.bio,
                EXISTS (
                    SELECT 1
                    FROM community_follows cf2
                    WHERE cf2.follower_student_id = ?
                      AND cf2.following_student_id = cp.student_id
                ) AS followed_by_me
            FROM community_follows cf
            JOIN community_profiles cp 
                ON cp.student_id = cf.follower_student_id
            WHERE cf.following_student_id = ?
            ORDER BY cf.created_at DESC
        `, [viewerStudentId, studentId]);

        return ok(res, { users });
    } catch (err) {
        console.error('getProfileFollowers error:', err);
        return fail(res, 'Server error', 500, err.message);
    }
};

const getProfileFollowing = async (req, res) => {
    try {
        const userId = getUserIdFromReq(req);
        const { studentId } = req.params;

        const [[viewer]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
            [userId]
        );

        if (!viewer) {
            return fail(res, 'Không tìm thấy sinh viên hiện tại', 404);
        }

        const viewerStudentId = viewer.student_id;

        const [users] = await db.query(`
            SELECT
                cp.student_id,
                cp.username,
                cp.display_name,
                cp.avatar_url,
                cp.bio,
                EXISTS (
                    SELECT 1
                    FROM community_follows cf2
                    WHERE cf2.follower_student_id = ?
                      AND cf2.following_student_id = cp.student_id
                ) AS followed_by_me
            FROM community_follows cf
            JOIN community_profiles cp 
                ON cp.student_id = cf.following_student_id
            WHERE cf.follower_student_id = ?
            ORDER BY cf.created_at DESC
        `, [viewerStudentId, studentId]);

        return ok(res, { users });
    } catch (err) {
        console.error('getProfileFollowing error:', err);
        return fail(res, 'Server error', 500, err.message);
    }
};

// ============================================================================
// NOTIFICATIONS
// ============================================================================

const createNotification = async (recipientStudentId, type, title, body, relatedPostId = null, relatedCommentId = null) => {
    await db.query(`
        INSERT INTO community_notifications
            (recipient_student_id, type, title, body, related_post_id, related_comment_id)
        VALUES (?, ?, ?, ?, ?, ?)
    `, [recipientStudentId, type, title, body || null, relatedPostId, relatedCommentId]);
};

// GET /api/community/notifications
const getNotifications = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [rows] = await db.query(`
            SELECT
                notification_id, type, title, body,
                related_post_id, related_comment_id, is_read, created_at
            FROM community_notifications
            WHERE recipient_student_id = ?
            ORDER BY created_at DESC
            LIMIT 100
        `, [student.student_id]);

        return ok(res, { notifications: rows, total: rows.length });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// GET /api/community/notifications/unread-count
const getUnreadNotificationCount = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [[{ count }]] = await db.query(`
            SELECT COUNT(*) AS count
            FROM community_notifications
            WHERE recipient_student_id = ? AND is_read = 0
        `, [student.student_id]);

        return ok(res, { count });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/notifications/:id/read
const markNotificationRead = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        await db.query(`
            UPDATE community_notifications SET is_read = 1
            WHERE notification_id = ? AND recipient_student_id = ?
        `, [req.params.id, student.student_id]);

        return ok(res, null, 'Đã đánh dấu đã đọc');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/notifications/read-all
const markAllNotificationsRead = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        await db.query(`
            UPDATE community_notifications SET is_read = 1
            WHERE recipient_student_id = ? AND is_read = 0
        `, [student.student_id]);

        return ok(res, null, 'Đã đọc tất cả');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// ============================================================================
// REPORTS
// ============================================================================

// POST /api/community/posts/:postId/report
const reportPost = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const { postId } = req.params;
        const { reason } = req.body;

        const [[post]] = await db.query('SELECT post_id FROM community_posts WHERE post_id = ?', [postId]);
        if (!post) return fail(res, 'Không tìm thấy bài viết', 404);

        const [[existing]] = await db.query(
            'SELECT report_id FROM post_reports WHERE post_id = ? AND reporter_student_id = ?',
            [postId, student.student_id]
        );
        if (existing) {
            await db.query(
                'UPDATE post_reports SET report_detail = ?, status = \'pending\', created_at = CURRENT_TIMESTAMP WHERE report_id = ?',
                [reason || null, existing.report_id]
            );
        } else {
            await db.query(
                'INSERT INTO post_reports (post_id, reporter_student_id, report_reason, report_detail) VALUES (?, ?, ?, ?)',
                [postId, student.student_id, 'other', reason || null]
            );
        }

        return ok(res, null, 'Đã gửi báo cáo bài viết');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/posts/:postId/comments/:commentId/report
const reportComment = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const { commentId } = req.params;
        const { reason } = req.body;

        const [[comment]] = await db.query('SELECT comment_id FROM comments WHERE comment_id = ?', [commentId]);
        if (!comment) return fail(res, 'Không tìm thấy bình luận', 404);

        const [[existing]] = await db.query(
            'SELECT report_id FROM comment_reports WHERE comment_id = ? AND reporter_student_id = ?',
            [commentId, student.student_id]
        );
        if (existing) {
            await db.query(
                'UPDATE comment_reports SET report_detail = ?, status = \'pending\', created_at = CURRENT_TIMESTAMP WHERE report_id = ?',
                [reason || null, existing.report_id]
            );
        } else {
            await db.query(
                'INSERT INTO comment_reports (comment_id, reporter_student_id, report_reason, report_detail) VALUES (?, ?, ?, ?)',
                [commentId, student.student_id, 'other', reason || null]
            );
        }

        return ok(res, null, 'Đã gửi báo cáo bình luận');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/posts/:postId/review-request
const createReviewRequest = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const { postId } = req.params;
        const { message } = req.body;

        const [[post]] = await db.query(
            'SELECT student_id, is_hidden FROM community_posts WHERE post_id = ?',
            [postId]
        );
        if (!post) return fail(res, 'Không tìm thấy bài viết', 404);
        if (post.student_id !== student.student_id)
            return fail(res, 'Chỉ tác giả mới có thể gửi yêu cầu xem xét', 403);
        if (!post.is_hidden)
            return fail(res, 'Bài viết này không bị ẩn', 400);

        const [[existing]] = await db.query(
            'SELECT request_id FROM post_review_requests WHERE post_id = ? AND student_id = ? AND status = \'pending\'',
            [postId, student.student_id]
        );
        if (existing) return fail(res, 'Bạn đã gửi yêu cầu xem xét cho bài này rồi', 400);

        await db.query(`
            INSERT INTO post_review_requests (post_id, student_id, message)
            VALUES (?, ?, ?)
        `, [postId, student.student_id, message || null]);

        return ok(res, null, 'Đã gửi yêu cầu xem xét đến quản trị viên');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// ============================================================================
// BLOCK / MUTE (author-based)
// ============================================================================

// POST /api/community/users/:studentId/block
const blockUser = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const { studentId } = req.params;
        if (parseInt(studentId) === student.student_id)
            return fail(res, 'Không thể chặn chính mình', 400);

        await db.query(
            'INSERT IGNORE INTO blocked_authors (student_id, blocked_student_id) VALUES (?, ?)',
            [student.student_id, studentId]
        );
        // Chặn thì cũng bỏ theo dõi hai chiều cho sạch feed.
        await db.query(
            'DELETE FROM community_follows WHERE (follower_student_id = ? AND following_student_id = ?) OR (follower_student_id = ? AND following_student_id = ?)',
            [student.student_id, studentId, studentId, student.student_id]
        );

        return ok(res, { blocked: true }, 'Đã chặn người dùng này');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// DELETE /api/community/users/:studentId/block
const unblockUser = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        await db.query(
            'DELETE FROM blocked_authors WHERE student_id = ? AND blocked_student_id = ?',
            [student.student_id, req.params.studentId]
        );

        return ok(res, { blocked: false }, 'Đã bỏ chặn');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// GET /api/community/profile/me/blocked
const getBlockedAuthors = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const [users] = await db.query(`
            SELECT
                cp.student_id, cp.username, cp.display_name,
                COALESCE(cp.avatar_url, u.avatar_url) AS avatar_url,
                cp.bio, ba.created_at
            FROM blocked_authors ba
            JOIN community_profiles cp ON cp.student_id = ba.blocked_student_id
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            WHERE ba.student_id = ?
            ORDER BY ba.created_at DESC
        `, [student.student_id]);

        return ok(res, { users });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// POST /api/community/users/:studentId/mute  (author-based)
const muteAuthorById = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        const { studentId } = req.params;
        if (parseInt(studentId) === student.student_id)
            return fail(res, 'Không thể mute chính mình', 400);

        await db.query(
            'INSERT IGNORE INTO MUTED_AUTHORS (student_id, muted_student_id) VALUES (?, ?)',
            [student.student_id, studentId]
        );
        return ok(res, { muted: true }, 'Đã ẩn bài viết từ tác giả này');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// DELETE /api/community/users/:studentId/mute
const unmuteAuthorById = async (req, res) => {
    try {
        const student = await getCurrentStudent(req.user.user_id);
        if (!student) return fail(res, 'Không tìm thấy sinh viên', 404);

        await db.query(
            'DELETE FROM MUTED_AUTHORS WHERE student_id = ? AND muted_student_id = ?',
            [student.student_id, req.params.studentId]
        );
        return ok(res, { muted: false }, 'Đã bỏ ẩn tác giả');
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// GET /api/community/topics/:topicId/posts
const getTopicPosts = async (req, res) => {
    req.query.filter = 'topic';
    req.query.topic_id = req.params.topicId;
    return getPosts(req, res);
};

// GET /api/community/profile/me/saved
const getMySavedPosts = getSavedPosts;

// GET /api/community/profile/:studentId/upvoted — bài viết mà student đó đã upvote
const getCommunityProfileUpvoted = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { studentId } = req.params;

        const [rows] = await db.query(`
            SELECT
                cp.post_id,
                cp.student_id,
                cp.title,
                cp.content,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,
                cp.topic_id,
                pt.topic_name,

                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE COALESCE(cprof.avatar_url, u.avatar_url) END AS author_avatar,

                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                COUNT(DISTINCT rp.repost_id) AS repost_count,

                EXISTS(
                    SELECT 1
                    FROM saved_posts sp2
                    WHERE sp2.post_id = cp.post_id
                    AND sp2.student_id = (
                        SELECT student_id
                        FROM students
                        WHERE user_id = ?
                        LIMIT 1
                    )
                ) AS is_saved,

                EXISTS(
                    SELECT 1
                    FROM post_reposts rpme
                    WHERE rpme.post_id = cp.post_id
                    AND rpme.student_id = (
                        SELECT student_id
                        FROM students
                        WHERE user_id = ?
                        LIMIT 1
                    )
                ) AS is_reposted

            FROM post_votes uv
            JOIN community_posts cp ON uv.post_id = cp.post_id
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN community_profiles cprof ON cp.student_id = cprof.student_id
            LEFT JOIN post_topics pt ON cp.topic_id = pt.topic_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id
            WHERE uv.student_id = ?
              AND uv.vote_type = 'upvote'
              AND cp.is_anonymous = 0
              AND cp.is_hidden = 0
            GROUP BY cp.post_id
            ORDER BY cp.created_at DESC
        `, [userId, userId, studentId]);

        return ok(res, { posts: rows, total: rows.length });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

module.exports = {
    getPosts,
    createPost,
    updatePost,
    deletePost,
    uploadProfileMusic,
    votePost,
    getPostDetail,
    createComment,
    voteComment,
    toggleSavePost,
    muteAuthor,
    getSavedPosts,
    getMySavedPosts,
    getCommunityProfileUpvoted,

    getMyCommunityProfile,
    getCommunityProfile,
    updateMyCommunityProfile,
    getCommunityProfilePosts,
    getCommunityProfileReplies,
    getCommunityProfileMedia,
    getCommunityProfileReposts,
    getProfileFollowers,
    getProfileFollowing,
    followUser,
    unfollowUser,
    toggleRepostPost,

    getPostTopics,
    getTopicPosts,

    // Notifications
    getNotifications,
    getUnreadNotificationCount,
    markNotificationRead,
    markAllNotificationsRead,
    createNotification,

    // Reports & review
    reportPost,
    reportComment,
    createReviewRequest,

    // Block / mute
    blockUser,
    unblockUser,
    getBlockedAuthors,
    muteAuthorById,
    unmuteAuthorById
};
