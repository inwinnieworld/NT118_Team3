const multer = require('multer');
const path = require('path');
const fs = require('fs');

const uploadDir = path.join(__dirname, '../../uploads/quests');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });

const allowedTypes = new Set([
    'image/jpeg',
    'image/png',
    'image/webp',
    'image/svg+xml',
    'video/mp4',
    'audio/mpeg',
    'audio/mp4',
    'audio/wav',
    'audio/x-wav',
    'audio/ogg'
]);

const extensionByMime = {
    'image/jpeg': '.jpg',
    'image/png': '.png',
    'image/webp': '.webp',
    'image/svg+xml': '.svg',
    'video/mp4': '.mp4',
    'audio/mpeg': '.mp3',
    'audio/mp4': '.m4a',
    'audio/wav': '.wav',
    'audio/x-wav': '.wav',
    'audio/ogg': '.ogg'
};

const storage = multer.diskStorage({
    destination: (_req, _file, callback) => callback(null, uploadDir),
    filename: (req, file, callback) => {
        const extension = extensionByMime[file.mimetype] || path.extname(file.originalname).toLowerCase();
        callback(null, `quest_${req.user.user_id}_${Date.now()}${extension}`);
    }
});

const fileFilter = (_req, file, callback) => {
    if (!allowedTypes.has(file.mimetype)) {
        return callback(new Error('Only JPG, PNG, WEBP, SVG, MP4, MP3, WAV and OGG files are allowed'));
    }
    callback(null, true);
};

module.exports = multer({
    storage,
    fileFilter,
    limits: { fileSize: 50 * 1024 * 1024 }
});
