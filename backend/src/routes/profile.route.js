const express = require('express');
const router = express.Router();
const authMiddleware = require('../middlewares/auth.middleware');
const upload = require('../middlewares/upload.middleware');
const { getProfile, updateProfile, uploadAvatar, changePassword } = require('../controllers/profile.controller');

router.use(authMiddleware);

router.get('/', getProfile);
router.put('/', updateProfile);
router.post('/avatar', upload.single('avatar'), uploadAvatar);
router.put('/change-password', changePassword);

module.exports = router;
