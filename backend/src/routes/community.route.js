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

// GET /api/community/posts?filter=new&page=1&search=abc&hashtag=Backend
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

// POST /api/community/posts/:postId/repost
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
// TOPICS / HASHTAGS
// =========================

// GET /api/community/topics
router.get('/topics', getPostTopics);

// =========================
// COMMUNITY PROFILE - ME
// =========================

// GET /api/community/profile/me
router.get('/profile/me', getMyCommunityProfile);

// PUT /api/community/profile/me
router.put('/profile/me', updateMyCommunityProfile);

// =========================
// COMMUNITY PROFILE - TABS
// Các route này phải đặt trước /profile/:studentId
// =========================

// GET /api/community/profile/:studentId/posts
router.get('/profile/:studentId/posts', getCommunityProfilePosts);

// GET /api/community/profile/:studentId/replies
router.get('/profile/:studentId/replies', getCommunityProfileReplies);

// GET /api/community/profile/:studentId/media
router.get('/profile/:studentId/media', getCommunityProfileMedia);

// GET /api/community/profile/:studentId/reposts
router.get('/profile/:studentId/reposts', getCommunityProfileReposts);

// GET /api/community/profile/:studentId/followers
router.get('/profile/:studentId/followers', getProfileFollowers);

// GET /api/community/profile/:studentId/following
router.get('/profile/:studentId/following', getProfileFollowing);

// =========================
// COMMUNITY PROFILE - DETAIL
// Route tổng quát này nên đặt sau các route tab ở trên
// =========================

// GET /api/community/profile/:studentId
router.get('/profile/:studentId', getCommunityProfile);

// =========================
// FOLLOW
// =========================

// POST /api/community/users/:studentId/follow
router.post('/users/:studentId/follow', followUser);

// DELETE /api/community/users/:studentId/follow
router.delete('/users/:studentId/follow', unfollowUser);

module.exports = router;