require("dotenv").config();
const express = require("express");
const cors = require("cors");
const path = require("path");

const authRoutes = require("./routes/auth.routes");
const profileRoutes = require("./routes/profile.route");
const adminRoutes = require("./routes/admin.route");
const gitJournalRoutes = require("./routes/gitjournal.routes");

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

// Placeholder for future routes
// app.use('/api/errorlog', require('./routes/errorlog.route'));
// app.use('/api/community', require('./routes/community.route'));

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
