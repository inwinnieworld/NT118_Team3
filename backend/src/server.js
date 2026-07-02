require("dotenv").config();
const express = require("express");
const cors = require("cors");
const path = require("path");

const authRoutes = require("./routes/auth.routes");
const profileRoutes = require("./routes/profile.route");
const adminRoutes = require("./routes/admin.route");
const gitJournalRoutes = require("./routes/gitjournal.routes");
const emergencyRoutes = require("./routes/emergency.routes");
const staffRoutes = require("./routes/staff.routes");
const communityRoutes = require("./routes/community.route");
const aichatRoutes = require("./routes/aichat.routes");
const chatRoutes = require('./routes/chat.routes');

const ragService = require("./services/rag.service");

const app = express();

app.use(cors());
app.use(express.json());

// Serve uploaded avatars as static files
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

app.get("/", (req, res) => {
  res.send("Emote Debugging backend is running");
});

// Authentication routes (register, login, forgot/reset password)
app.use("/api/auth", authRoutes);

// Profile routes (view, update, avatar, change password)
app.use("/api/profile", profileRoutes);

// Admin routes (manage students, staff)
app.use("/api/admin", adminRoutes);

// Git Journal routes (commits, merges, graph)
app.use("/api/gitjournal", gitJournalRoutes);

// Emergency and Staff routes
app.use("/api/emergency", emergencyRoutes);
app.use("/api/staff", staffRoutes);

// Community routes
app.use("/api/community", communityRoutes);
// Chat routes
app.use("/api/chat", chatRoutes);

// AI Chat routes (Dr.Bug)
app.use("/api/aichat", aichatRoutes);

// Quest builder routes (visual flow engine, approval, runtime sessions)
app.use("/api/quest-builder", require("./routes/questBuilder.route"));

// Placeholder for future routes
// app.use('/api/errorlog', require('./routes/errorlog.route'));

const PORT = process.env.PORT || 3000;
const http = require('http');
const { Server } = require('socket.io');
const jwt = require('jsonwebtoken');
const db = require('./config/db');

const server = http.createServer(app);

const io = new Server(server, {
    cors: {
        origin: '*',
        methods: ['GET', 'POST']
    }
});

io.use(async (socket, next) => {
    try {
        const token = socket.handshake.auth?.token;

        if (!token) {
            return next(new Error('Missing token'));
        }

        const rawToken = token.replace('Bearer ', '');
        const decoded = jwt.verify(rawToken, process.env.JWT_SECRET);

        console.log('SOCKET decoded token:', decoded);

        const userId =
            decoded.user_id ||
            decoded.userId ||
            decoded.id ||
            decoded.user?.user_id;

        if (!userId) {
            return next(new Error('Token missing user_id'));
        }

        const [[student]] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ? LIMIT 1',
            [userId]
        );

        if (!student) {
            console.log('SOCKET student not found for user_id:', userId);
            return next(new Error('Student not found'));
        }

        socket.userId = userId;
        socket.studentId = student.student_id;

        socket.join(`student:${student.student_id}`);

        console.log('Socket connected student_id:', student.student_id);

        next();
    } catch (err) {
        console.error('Socket auth error:', err.message);
        next(new Error(err.message || 'Unauthorized'));
    }
});

io.on('connection', (socket) => {
    console.log('Socket connected:', socket.studentId);

    socket.on('chat:send', async (payload, callback) => {
        try {
            const senderStudentId = socket.studentId;
            const receiverStudentId = Number(payload.receiver_student_id);
            const messageText = String(payload.message_text || '').trim();

            if (!receiverStudentId || !messageText) {
                if (callback) callback({ success: false, message: 'Dữ liệu không hợp lệ' });
                return;
            }

            const lowId = Math.min(senderStudentId, receiverStudentId);
            const highId = Math.max(senderStudentId, receiverStudentId);

            await db.query(`
                INSERT INTO chat_conversations 
                    (student_low_id, student_high_id, last_message, last_message_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE 
                    last_message = VALUES(last_message),
                    last_message_at = CURRENT_TIMESTAMP
            `, [lowId, highId, messageText]);

            const [[conversation]] = await db.query(`
                SELECT conversation_id 
                FROM chat_conversations
                WHERE student_low_id = ? AND student_high_id = ?
                LIMIT 1
            `, [lowId, highId]);

            const [result] = await db.query(`
                INSERT INTO chat_messages 
                    (conversation_id, sender_student_id, receiver_student_id, message_text)
                VALUES (?, ?, ?, ?)
            `, [
                conversation.conversation_id,
                senderStudentId,
                receiverStudentId,
                messageText
            ]);

            const message = {
                message_id: result.insertId,
                conversation_id: conversation.conversation_id,
                sender_student_id: senderStudentId,
                receiver_student_id: receiverStudentId,
                message_text: messageText,
                created_at: new Date().toISOString()
            };

            io.to(`student:${senderStudentId}`).emit('chat:new_message', message);
            io.to(`student:${receiverStudentId}`).emit('chat:new_message', message);

            if (callback) callback({ success: true, data: message });
        } catch (err) {
            console.error('chat:send error:', err);
            if (callback) callback({ success: false, message: err.message });
        }
    });

    socket.on('disconnect', () => {
        console.log('Socket disconnected:', socket.studentId);
    });
});

server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);

    // Khởi động RAG nền: tải model embedding + tính vector cho cây problems.
    // Chạy bất đồng bộ để không chặn server; lần chat đầu sẽ chờ init xong nếu chưa kịp.
    ragService.init().catch((err) => {
        console.error('[RAG] Khởi tạo thất bại (chat sẽ thử lại khi có request):', err.message);
    });
});
