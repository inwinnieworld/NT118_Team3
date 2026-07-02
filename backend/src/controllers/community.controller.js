const db = require('../config/db');
const { ok, fail } = require('../utils/response');

const normalizePostRows = (rows) => {
    return rows.map(row => ({
        ...row,
        is_saved: row.is_saved ? 1 : 0,
        is_reposted: row.is_reposted ? 1 : 0,
        hashtags: row.hashtag_csv
            ? row.hashtag_csv.split(',').map(tag => tag.trim()).filter(Boolean)
            : []
    }));
};

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
                cp.avatar_url,
                cp.cover_url,
                cp.bio,
                cp.follower_count,
                cp.following_count,
                cp.post_count,
                s.major,
                s.faculty,
                s.year_of_study,
                TRUE AS is_me,
                FALSE AS followed_by_me
            FROM community_profiles cp
            JOIN students s ON cp.student_id = s.student_id
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
                cp.avatar_url,
                cp.cover_url,
                cp.bio,
                cp.follower_count,
                cp.following_count,
                cp.post_count,
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
            bio
        } = req.body;

        const student = await getCurrentStudent(userId);
        if (!student) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        if (!username || username.trim().length < 3) {
            return fail(res, 'Username phải có ít nhất 3 ký tự', 400);
        }

        if (!display_name || display_name.trim().length < 2) {
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
                updated_at = CURRENT_TIMESTAMP
            WHERE student_id = ?
        `, [
            username.trim(),
            display_name.trim(),
            avatar_url || null,
            cover_url || null,
            bio || null,
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

const getCommunityProfilePosts = async (req, res) => {
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

                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.avatar_url END AS author_avatar,

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
                    FROM post_reposts rp2
                    WHERE rp2.post_id = cp.post_id
                    AND rp2.student_id = (
                        SELECT student_id
                        FROM students
                        WHERE user_id = ?
                        LIMIT 1
                    )
                ) AS is_reposted,

                GROUP_CONCAT(DISTINCT pt.topic_name ORDER BY pt.topic_name SEPARATOR ',') AS hashtag_csv

            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id
            LEFT JOIN post_topic_mapping ptm ON cp.post_id = ptm.post_id
            LEFT JOIN post_topics pt ON ptm.topic_id = pt.topic_id

            WHERE cp.student_id = ?
              AND cp.is_anonymous = 0
            GROUP BY cp.post_id
            ORDER BY cp.created_at DESC
        `, [userId, userId, studentId]);

        const posts = normalizePostRows(rows);

        return ok(res, { posts, total: posts.length });
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
        const userId = req.user.user_id;
        const { studentId } = req.params;

        const currentStudent = await getCurrentStudent(userId);

        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        const [rows] = await db.query(`
            SELECT 
                cp.post_id,
                cp.student_id,
                cp.title,
                cp.content,
                cp.image_url,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,

                CASE 
                    WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' 
                    ELSE u.name 
                END AS author_name,

                CASE 
                    WHEN cp.is_anonymous = 1 THEN NULL 
                    ELSE u.avatar_url 
                END AS author_avatar,

                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                COUNT(DISTINCT rp.repost_id) AS repost_count,

                EXISTS(
                    SELECT 1 
                    FROM saved_posts sp2 
                    WHERE sp2.post_id = cp.post_id 
                      AND sp2.student_id = ?
                ) AS is_saved,

                EXISTS(
                    SELECT 1
                    FROM post_reposts rp2
                    WHERE rp2.post_id = cp.post_id
                      AND rp2.student_id = ?
                ) AS is_reposted,

                GROUP_CONCAT(DISTINCT pt.topic_name ORDER BY pt.topic_name SEPARATOR ',') AS hashtag_csv

            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id
            LEFT JOIN post_topic_mapping ptm ON cp.post_id = ptm.post_id
            LEFT JOIN post_topics pt ON ptm.topic_id = pt.topic_id

            WHERE cp.student_id = ?
              AND cp.is_anonymous = 0
              AND cp.image_url IS NOT NULL
              AND cp.image_url <> ''

            GROUP BY cp.post_id
            ORDER BY cp.created_at DESC
        `, [
            currentStudent.student_id,
            currentStudent.student_id,
            studentId
        ]);

        const posts = normalizePostRows(rows);

        return ok(res, {
            posts,
            total: posts.length
        });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getCommunityProfileReposts = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { studentId } = req.params;

        const currentStudent = await getCurrentStudent(userId);

        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
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

                pr.created_at AS reposted_at,

                CASE 
                    WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' 
                    ELSE u.name 
                END AS author_name,

                CASE 
                    WHEN cp.is_anonymous = 1 THEN NULL 
                    ELSE u.avatar_url 
                END AS author_avatar,

                COUNT(DISTINCT CASE WHEN v.vote_type = 'upvote' THEN v.vote_id END) AS upvote_count,
                COUNT(DISTINCT CASE WHEN v.vote_type = 'downvote' THEN v.vote_id END) AS downvote_count,
                COUNT(DISTINCT c.comment_id) AS comment_count,
                COUNT(DISTINCT rp_all.repost_id) AS repost_count,

                EXISTS(
                    SELECT 1 
                    FROM saved_posts sp2 
                    WHERE sp2.post_id = cp.post_id 
                      AND sp2.student_id = ?
                ) AS is_saved,

                EXISTS(
                    SELECT 1
                    FROM post_reposts rp2
                    WHERE rp2.post_id = cp.post_id
                      AND rp2.student_id = ?
                ) AS is_reposted,

                GROUP_CONCAT(DISTINCT pt.topic_name ORDER BY pt.topic_name SEPARATOR ',') AS hashtag_csv

            FROM post_reposts pr
            JOIN community_posts cp ON pr.post_id = cp.post_id

            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id

            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp_all ON cp.post_id = rp_all.post_id

            LEFT JOIN post_topic_mapping ptm ON cp.post_id = ptm.post_id
            LEFT JOIN post_topics pt ON ptm.topic_id = pt.topic_id

            WHERE pr.student_id = ?

            GROUP BY cp.post_id, pr.created_at
            ORDER BY pr.created_at DESC
        `, [
            currentStudent.student_id,
            currentStudent.student_id,
            studentId
        ]);

        const posts = normalizePostRows(rows);

        return ok(res, {
            posts,
            total: posts.length
        });
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

        await db.query(`
            INSERT IGNORE INTO community_follows 
            (follower_student_id, following_student_id)
            VALUES (?, ?)
        `, [currentStudent.student_id, studentId]);

        await db.query(`
            UPDATE community_profiles 
            SET following_count = (
                SELECT COUNT(*) 
                FROM community_follows 
                WHERE follower_student_id = ?
            )
            WHERE student_id = ?
        `, [currentStudent.student_id, currentStudent.student_id]);

        await db.query(`
            UPDATE community_profiles 
            SET follower_count = (
                SELECT COUNT(*) 
                FROM community_follows 
                WHERE following_student_id = ?
            )
            WHERE student_id = ?
        `, [studentId, studentId]);

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

        await db.query(`
            DELETE FROM community_follows
            WHERE follower_student_id = ?
              AND following_student_id = ?
        `, [currentStudent.student_id, studentId]);

        await db.query(`
            UPDATE community_profiles 
            SET following_count = (
                SELECT COUNT(*) 
                FROM community_follows 
                WHERE follower_student_id = ?
            )
            WHERE student_id = ?
        `, [currentStudent.student_id, currentStudent.student_id]);

        await db.query(`
            UPDATE community_profiles 
            SET follower_count = (
                SELECT COUNT(*) 
                FROM community_follows 
                WHERE following_student_id = ?
            )
            WHERE student_id = ?
        `, [studentId, studentId]);

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


/// GET /api/community/posts?filter=new&page=1&search=&hashtag=
const getPosts = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = 20;
        const offset = (page - 1) * limit;

        const filter = req.query.filter || 'new'; // new | trending | best | my_logs
        const search = req.query.search ? `%${req.query.search}%` : '%';
        const hashtag = req.query.hashtag ? req.query.hashtag.trim() : null;

        const userId = req.user.user_id;
        const currentStudent = await getCurrentStudent(userId);

        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        let orderBy = 'cp.created_at DESC';
        let extraWhere = '';
        const params = [userId, userId, search, search, currentStudent.student_id];

        if (filter === 'trending') {
            orderBy = 'upvote_count DESC, cp.created_at DESC';
        } else if (filter === 'best') {
            orderBy = 'comment_count DESC, upvote_count DESC';
        } else if (filter === 'my_logs') {
            extraWhere += ' AND s.user_id = ?';
            params.push(userId);
        }

        if (hashtag) {
            extraWhere += `
                AND EXISTS (
                    SELECT 1
                    FROM post_topic_mapping ptm_filter
                    JOIN post_topics pt_filter 
                        ON ptm_filter.topic_id = pt_filter.topic_id
                    WHERE ptm_filter.post_id = cp.post_id
                      AND (
                          pt_filter.topic_name = ?
                          OR REPLACE(LOWER(pt_filter.topic_name), ' ', '-') = REPLACE(LOWER(?), ' ', '-')
                      )
                )
            `;
            params.push(hashtag, hashtag);
        }

        params.push(limit, offset);

        const [rows] = await db.query(`
            SELECT 
                cp.post_id,
                cp.student_id,
                cp.title,
                cp.content,
                cp.is_anonymous,
                cp.created_at,
                cp.view_count,

                CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author_name,
                CASE WHEN cp.is_anonymous = 1 THEN NULL ELSE u.avatar_url END AS author_avatar,

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
                    FROM post_reposts rp2
                    WHERE rp2.post_id = cp.post_id
                    AND rp2.student_id = (
                        SELECT student_id
                        FROM students
                        WHERE user_id = ?
                        LIMIT 1
                    )
                ) AS is_reposted,

                GROUP_CONCAT(DISTINCT pt.topic_name ORDER BY pt.topic_name SEPARATOR ',') AS hashtag_csv

            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id

            LEFT JOIN post_topic_mapping ptm ON cp.post_id = ptm.post_id
            LEFT JOIN post_topics pt ON ptm.topic_id = pt.topic_id

            WHERE (cp.title LIKE ? OR cp.content LIKE ?)
                AND NOT EXISTS (
                    SELECT 1
                    FROM muted_authors ma
                    WHERE ma.student_id = ?
                        AND ma.muted_student_id = cp.student_id
                )
                ${extraWhere}
            GROUP BY cp.post_id
            ORDER BY ${orderBy}
            LIMIT ? OFFSET ?
        `, params);

        const countParams = [search, search, currentStudent.student_id];

        let countExtraWhere = '';

        if (filter === 'my_logs') {
            countExtraWhere += ' AND s.user_id = ?';
            countParams.push(userId);
        }

        if (hashtag) {
            countExtraWhere += `
                AND EXISTS (
                    SELECT 1
                    FROM post_topic_mapping ptm_filter
                    JOIN post_topics pt_filter 
                        ON ptm_filter.topic_id = pt_filter.topic_id
                    WHERE ptm_filter.post_id = cp.post_id
                      AND (
                          pt_filter.topic_name = ?
                          OR REPLACE(LOWER(pt_filter.topic_name), ' ', '-') = REPLACE(LOWER(?), ' ', '-')
                      )
                )
            `;
            countParams.push(hashtag, hashtag);
        }

        const [[{ total }]] = await db.query(`
            SELECT COUNT(DISTINCT cp.post_id) as total
            FROM community_posts cp
            LEFT JOIN students s ON cp.student_id = s.student_id
            WHERE (cp.title LIKE ? OR cp.content LIKE ?)
                AND NOT EXISTS (
                    SELECT 1
                    FROM muted_authors ma
                    WHERE ma.student_id = ?
                        AND ma.muted_student_id = cp.student_id
                )
                ${countExtraWhere}
        `, countParams);

        const posts = normalizePostRows(rows);

        return ok(res, {
            posts,
            total,
            page,
            totalPages: Math.ceil(total / limit)
        });
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

// REMOVED: getErrorTypes - Community không cần error_types (thuộc Error Log System)

// POST /api/community/posts — đăng bài mới
const createPost = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const { title, content, is_anonymous, hashtags } = req.body;

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
        if (Array.isArray(hashtags) && hashtags.length > 0) {
            for (const rawTag of hashtags) {
                const tagName = String(rawTag || '')
                    .trim()
                    .replace(/^#/, '');

                if (!tagName) continue;

                await db.query(`
                    INSERT IGNORE INTO post_topics (topic_name)
                    VALUES (?)
                `, [tagName]);

                const [[topic]] = await db.query(`
                    SELECT topic_id 
                    FROM post_topics 
                    WHERE topic_name = ? 
                    LIMIT 1
                `, [tagName]);

                if (topic) {
                    await db.query(`
                        INSERT IGNORE INTO post_topic_mapping (post_id, topic_id)
                        VALUES (?, ?)
                    `, [postId, topic.topic_id]);
                }
            }
        }

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
        const [tagRows] = await db.query(`
            SELECT pt.topic_name
            FROM post_topic_mapping ptm
            JOIN post_topics pt ON ptm.topic_id = pt.topic_id
            WHERE ptm.post_id = ?
            ORDER BY pt.topic_name ASC
        `, [postId]);

        post.hashtags = tagRows.map(row => row.topic_name);

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

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
            [userId]
        );

        if (!student) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        const [[post]] = await db.query(
            'SELECT student_id FROM community_posts WHERE post_id = ? LIMIT 1',
            [postId]
        );

        if (!post) {
            return fail(res, 'Không tìm thấy bài viết', 404);
        }

        if (post.student_id === student.student_id) {
            return fail(res, 'Không thể mute chính mình', 400);
        }

        await db.query(
            'INSERT IGNORE INTO muted_authors (student_id, muted_student_id) VALUES (?, ?)',
            [student.student_id, post.student_id]
        );

        return ok(
            res,
            {
                muted_student_id: post.student_id
            },
            'Đã ẩn bài viết từ tác giả này'
        );
    } catch (err) {
        return fail(res, 'Server error', 500, err.message);
    }
};

const getSavedPosts = async (req, res) => {
    try {
        const userId = req.user.user_id;
        const currentStudent = await getCurrentStudent(userId);

        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
        }

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ?',
            [userId]
        );

        if (!student) {
            return fail(res, 'Không tìm thấy sinh viên', 404);
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
                    FROM post_reposts rp2
                    WHERE rp2.post_id = cp.post_id
                    AND rp2.student_id = ?
                ) AS is_reposted,

                GROUP_CONCAT(DISTINCT pt.topic_name ORDER BY pt.topic_name SEPARATOR ',') AS hashtag_csv

            FROM saved_posts sp
            JOIN community_posts cp ON sp.post_id = cp.post_id
            LEFT JOIN students s ON cp.student_id = s.student_id
            LEFT JOIN users u ON s.user_id = u.user_id
            LEFT JOIN post_votes v ON cp.post_id = v.post_id
            LEFT JOIN comments c ON cp.post_id = c.post_id
            LEFT JOIN post_reposts rp ON cp.post_id = rp.post_id
            LEFT JOIN post_topic_mapping ptm ON cp.post_id = ptm.post_id
            LEFT JOIN post_topics pt ON ptm.topic_id = pt.topic_id

            WHERE sp.student_id = ?
            GROUP BY cp.post_id
            ORDER BY sp.saved_at DESC
        `, [student.student_id, student.student_id]);

        const posts = normalizePostRows(rows);

        return ok(res, { posts, total: posts.length });
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

module.exports = {
    getPosts,
    createPost,
    votePost,
    getPostDetail,
    createComment,
    voteComment,
    toggleSavePost,
    muteAuthor,
    getSavedPosts,

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

    getPostTopics
};
