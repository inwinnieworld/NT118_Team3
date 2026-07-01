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

// AI Chat routes (Dr.Bug)
app.use("/api/aichat", aichatRoutes);

// Quest builder routes (visual flow engine, approval, runtime sessions)
app.use("/api/quest-builder", require("./routes/questBuilder.route"));

// Placeholder for future routes
// app.use('/api/errorlog', require('./routes/errorlog.route'));

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);

  // Khởi động RAG nền: tải model embedding + tính vector cho cây problems.
  // Chạy bất đồng bộ để không chặn server; lần chat đầu sẽ chờ init xong nếu chưa kịp.
  ragService.init().catch((err) => {
    console.error('[RAG] Khởi tạo thất bại (chat sẽ thử lại khi có request):', err.message);
  });
});
