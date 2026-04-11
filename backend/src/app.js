require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();

app.use(cors());
app.use(express.json());

// Serve uploaded avatars as static files
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// Routes
app.use('/api/auth', require('./routes/auth.route'));
app.use('/api/profile', require('./routes/profile.route'));
app.use('/api/admin', require('./routes/admin.route'));
// Các route khác của team thêm vào đây:
// app.use('/api/errorlog', require('./routes/errorlog.route'));
// app.use('/api/community', require('./routes/community.route'));

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
