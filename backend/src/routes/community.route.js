const express = require('express');
const router = express.Router();
const authMiddleware = require('../middlewares/auth.middleware');
const {
    getPosts,
    getErrorTypes,
    createPost,
    votePost,
    acceptQuestSuggestion,
    getPostDetail,
    createComment,
    voteComment,
    toggleSavePost,
    muteAuthor,
    getSavedPosts
} = require('../controllers/community.controller');

router.use(authMiddleware);

router.get('/posts', getPosts);
router.get('/saved', getSavedPosts);
router.get('/error-types', getErrorTypes);
router.post('/posts', createPost);
router.get('/posts/:postId', getPostDetail);
router.post('/posts/:postId/vote', votePost);
router.post('/posts/:postId/save', toggleSavePost);
router.post('/posts/:postId/mute', muteAuthor);
router.post('/posts/:postId/comments', createComment);
router.post('/posts/:postId/comments/:commentId/vote', voteComment);
router.post('/quests/accept', acceptQuestSuggestion);

module.exports = router;
