const express = require('express');
const router = express.Router();

const authMiddleware = require('../middlewares/auth.middleware');
const musicUpload = require('../middlewares/musicUpload.middleware');

const {
    getPosts,
    createPost,
    updatePost,
    deletePost,
    uploadProfileMusic,
    votePost,
    getPostDetail,
    createComment,
    voteComment,
    toggleSavePost,
    muteAuthor,
    getSavedPosts,
    getMySavedPosts,
    getCommunityProfileUpvoted,

    // Topics
    getPostTopics,
    getTopicPosts,

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
    unfollowUser,

    // Notifications
    getNotifications,
    getUnreadNotificationCount,
    markNotificationRead,
    markAllNotificationsRead,

    // Reports & review
    reportPost,
    reportComment,
    createReviewRequest,

    // Block / mute
    blockUser,
    unblockUser,
    getBlockedAuthors,
    muteAuthorById,
    unmuteAuthorById
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

// PUT /api/community/posts/:postId — sửa bài (chỉ chủ bài)
router.put('/posts/:postId', updatePost);

// DELETE /api/community/posts/:postId — xóa bài (chỉ chủ bài)
router.delete('/posts/:postId', deletePost);

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

// POST /api/community/profile/me/music — upload mp3
router.post('/profile/me/music', musicUpload.single('music'), uploadProfileMusic);

// GET /api/community/profile/me/saved — bài viết đã lưu (đặt TRƯỚC /profile/:studentId)
router.get('/profile/me/saved', getMySavedPosts);

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
router.get('/profile/:studentId/upvoted', getCommunityProfileUpvoted);

// =========================
// NOTIFICATIONS
// =========================

// GET /api/community/notifications
router.get('/notifications', getNotifications);

// GET /api/community/notifications/unread-count
router.get('/notifications/unread-count', getUnreadNotificationCount);

// POST /api/community/notifications/read-all
router.post('/notifications/read-all', markAllNotificationsRead);

// POST /api/community/notifications/:id/read
router.post('/notifications/:id/read', markNotificationRead);

// =========================
// REPORTS & REVIEW REQUEST
// =========================

// POST /api/community/posts/:postId/report
router.post('/posts/:postId/report', reportPost);

// POST /api/community/posts/:postId/comments/:commentId/report
router.post('/posts/:postId/comments/:commentId/report', reportComment);

// POST /api/community/posts/:postId/review-request
router.post('/posts/:postId/review-request', createReviewRequest);

// =========================
// BLOCK / MUTE (author-based)
// =========================

// POST /api/community/users/:studentId/block
router.post('/users/:studentId/block', blockUser);

// DELETE /api/community/users/:studentId/block
router.delete('/users/:studentId/block', unblockUser);

// GET /api/community/profile/me/blocked
router.get('/profile/me/blocked', getBlockedAuthors);

// POST /api/community/users/:studentId/mute
router.post('/users/:studentId/mute', muteAuthorById);

// DELETE /api/community/users/:studentId/mute
router.delete('/users/:studentId/mute', unmuteAuthorById);


module.exports = router;