const express = require('express');
const router = express.Router();

const authMiddleware = require('../middlewares/auth.middleware');

const {
    getPosts,
    createPost,
    votePost,
    getPostDetail,
    createComment,
    voteComment,
    toggleSavePost,
    muteAuthor,
    getSavedPosts,

    // Topics
    getPostTopics,

    // Community Profile
    getMyCommunityProfile,
    getCommunityProfile,
    updateMyCommunityProfile,
    getCommunityProfilePosts,
    getCommunityProfileReplies,
    getCommunityProfileMedia,
    getCommunityProfileReposts,
    toggleRepostPost,
    getProfileFollowers,
    getProfileFollowing,

    // Follow
    followUser,
    unfollowUser
} = require('../controllers/community.controller');

// Tất cả route community đều yêu cầu đăng nhập
router.use(authMiddleware);

// =========================
// POSTS
// =========================

// GET /api/community/posts?filter=new&page=1&search=abc
router.get('/posts', getPosts);

// POST /api/community/posts
router.post('/posts', createPost);

// GET /api/community/posts/:postId
router.get('/posts/:postId', getPostDetail);

// POST /api/community/posts/:postId/vote
router.post('/posts/:postId/vote', votePost);

// POST /api/community/posts/:postId/save
router.post('/posts/:postId/save', toggleSavePost);

// POST /api/community/posts/:postId/mute
router.post('/posts/:postId/mute', muteAuthor);
router.post('/posts/:postId/repost', toggleRepostPost);

// =========================
// COMMENTS
// =========================

// POST /api/community/posts/:postId/comments
router.post('/posts/:postId/comments', createComment);

// POST /api/community/posts/:postId/comments/:commentId/vote
router.post('/posts/:postId/comments/:commentId/vote', voteComment);

// =========================
// SAVED POSTS
// =========================

// GET /api/community/saved
router.get('/saved', getSavedPosts);

// =========================
// TOPICS
// =========================

// GET /api/community/topics
router.get('/topics', getPostTopics);

// =========================
// COMMUNITY PROFILE
// =========================

// GET /api/community/profile/me
router.get('/profile/me', getMyCommunityProfile);

// PUT /api/community/profile/me
router.put('/profile/me', updateMyCommunityProfile);

// Lưu ý: route /profile/:studentId/posts phải đặt TRƯỚC /profile/:studentId
// GET /api/community/profile/:studentId/posts
router.get('/profile/:studentId/posts', getCommunityProfilePosts);

// GET /api/community/profile/:studentId
router.get('/profile/:studentId', getCommunityProfile);

router.get('/profile/:studentId/followers', getProfileFollowers);
router.get('/profile/:studentId/following', getProfileFollowing);

// =========================
// FOLLOW
// =========================

// POST /api/community/users/:studentId/follow
router.post('/users/:studentId/follow', followUser);

// DELETE /api/community/users/:studentId/follow
router.delete('/users/:studentId/follow', unfollowUser);

router.get('/profile/:studentId/posts', getCommunityProfilePosts);
router.get('/profile/:studentId/replies', getCommunityProfileReplies);
router.get('/profile/:studentId/media', getCommunityProfileMedia);
router.get('/profile/:studentId/reposts', getCommunityProfileReposts);


module.exports = router;