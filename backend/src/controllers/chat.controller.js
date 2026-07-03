const db = require('../config/db');
const { ok, fail } = require('../utils/response');

const getUserIdFromReq = (req) => {
    return (
        req.user?.user_id ||
        req.user?.userId ||
        req.user?.id ||
        req.user?.user?.user_id ||
        req.user?.user?.id ||
        req.userId
    );
};

const getCurrentStudent = async (userId) => {
    const [[student]] = await db.query(
        `
        SELECT student_id
        FROM students
        WHERE user_id = ?
        LIMIT 1
        `,
        [userId]
    );

    return student;
};

const getMessagesWithUser = async (req, res) => {
    try {
        const userId = getUserIdFromReq(req);
        const { studentId } = req.params;

        console.log('CHAT HISTORY req.user =', req.user);
        console.log('CHAT HISTORY userId =', userId);
        console.log('CHAT HISTORY targetStudentId =', studentId);

        if (!userId) {
            return fail(res, 'Token missing user_id', 401);
        }

        const currentStudent = await getCurrentStudent(userId);

        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên hiện tại', 404);
        }

        const currentStudentId = Number(currentStudent.student_id);
        const targetStudentId = Number(studentId);

        if (!targetStudentId) {
            return fail(res, 'studentId không hợp lệ', 400);
        }

        const lowId = Math.min(currentStudentId, targetStudentId);
        const highId = Math.max(currentStudentId, targetStudentId);

        const [[conversation]] = await db.query(
            `
            SELECT conversation_id
            FROM chat_conversations
            WHERE student_low_id = ?
              AND student_high_id = ?
            LIMIT 1
            `,
            [lowId, highId]
        );

        if (!conversation) {
            return ok(res, {
                current_student_id: currentStudentId,
                target_student_id: targetStudentId,
                messages: []
            });
        }

        const [messages] = await db.query(
            `
            SELECT
                message_id,
                conversation_id,
                sender_student_id,
                receiver_student_id,
                message_text,
                is_read,
                created_at
            FROM chat_messages
            WHERE conversation_id = ?
            ORDER BY created_at ASC, message_id ASC
            `,
            [conversation.conversation_id]
        );

        return ok(res, {
            current_student_id: currentStudentId,
            target_student_id: targetStudentId,
            messages
        });
    } catch (err) {
        console.error('getMessagesWithUser error:', err);
        return fail(res, 'Server error', 500, err.message);
    }
};

const getConversations = async (req, res) => {
    try {
        const userId = getUserIdFromReq(req);

        console.log('CHAT CONVERSATIONS req.user =', req.user);
        console.log('CHAT CONVERSATIONS userId =', userId);

        if (!userId) {
            return fail(res, 'Token missing user_id', 401);
        }

        const currentStudent = await getCurrentStudent(userId);

        if (!currentStudent) {
            return fail(res, 'Không tìm thấy sinh viên hiện tại', 404);
        }

        const currentStudentId = Number(currentStudent.student_id);

        console.log('CHAT CONVERSATIONS currentStudentId =', currentStudentId);

        const [rows] = await db.query(
            `
            SELECT
                other_student.student_id,

                COALESCE(
                    cp.display_name,
                    u.name,
                    CONCAT('User ', other_student.student_id)
                ) AS display_name,

                COALESCE(
                    cp.username,
                    LOWER(REPLACE(u.name, ' ', '')),
                    u.email
                ) AS username,

                COALESCE(
                    cp.avatar_url,
                    u.avatar_url
                ) AS avatar_url,

                UPPER(
                    LEFT(
                        COALESCE(cp.display_name, u.name, 'U'),
                        1
                    )
                ) AS avatar_text,

                last_message.message_text AS last_message,
                last_message.created_at AS last_message_at,

                COALESCE(unread.unread_count, 0) AS unread_count,
                COALESCE(cp.follower_count, 0) AS follower_count,

                CASE
                    WHEN cf.id IS NULL THEN 0
                    ELSE 1
                END AS followed_by_me

            FROM chat_conversations c

            INNER JOIN chat_messages last_message
                ON last_message.message_id = (
                    SELECT cm.message_id
                    FROM chat_messages cm
                    WHERE cm.conversation_id = c.conversation_id
                    ORDER BY cm.created_at DESC, cm.message_id DESC
                    LIMIT 1
                )

            INNER JOIN students other_student
                ON other_student.student_id =
                    CASE
                        WHEN c.student_low_id = ? THEN c.student_high_id
                        ELSE c.student_low_id
                    END

            INNER JOIN users u
                ON u.user_id = other_student.user_id

            LEFT JOIN community_profiles cp
                ON cp.student_id = other_student.student_id

            LEFT JOIN community_follows cf
                ON cf.follower_student_id = ?
               AND cf.following_student_id = other_student.student_id

            LEFT JOIN (
                SELECT
                    conversation_id,
                    COUNT(*) AS unread_count
                FROM chat_messages
                WHERE receiver_student_id = ?
                  AND is_read = 0
                GROUP BY conversation_id
            ) unread
                ON unread.conversation_id = c.conversation_id

            WHERE c.student_low_id = ?
               OR c.student_high_id = ?

            ORDER BY last_message.created_at DESC, last_message.message_id DESC
            `,
            [
                currentStudentId,
                currentStudentId,
                currentStudentId,
                currentStudentId,
                currentStudentId
            ]
        );

        const conversations = rows.map(row => ({
            student_id: Number(row.student_id),

            display_name: row.display_name || 'Người dùng',
            username: row.username || '',
            avatar_url: row.avatar_url || null,
            avatar_text: row.avatar_text || 'U',

            last_message: row.last_message || '',
            last_message_at: row.last_message_at,

            unread_count: Number(row.unread_count || 0),
            follower_count: Number(row.follower_count || 0),

            // Quan trọng: Android đang cần boolean, không phải 0/1
            followed_by_me: row.followed_by_me === 1 || row.followed_by_me === true
        }));

        return ok(res, {
            conversations
        });
    } catch (err) {
        console.error('getConversations error:', err);

        return fail(
            res,
            'Lỗi server khi lấy danh sách tin nhắn',
            500,
            err.message
        );
    }
};

module.exports = {
    getMessagesWithUser,
    getConversations
};