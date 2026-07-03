const multer = require('multer');
const path = require('path');
const fs = require('fs');

const uploadDir = path.join(__dirname, '../../uploads/music');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });

const allowedTypes = new Set(['audio/mpeg']);

const storage = multer.diskStorage({
    destination: (_req, _file, callback) => callback(null, uploadDir),
    filename: (req, file, callback) => {
        callback(null, `music_${req.user.user_id}_${Date.now()}.mp3`);
    }
});

const fileFilter = (_req, file, callback) => {
    if (!allowedTypes.has(file.mimetype)) {
        return callback(new Error('Chỉ chấp nhận file MP3'));
    }
    callback(null, true);
};

module.exports = multer({
    storage,
    fileFilter,
    limits: { fileSize: 15 * 1024 * 1024 }
});
