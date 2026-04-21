const express = require('express');
const router = express.Router();
const gitJournalController = require('../controllers/gitjournal.controller');
const authenticateToken = require('../middlewares/auth.middleware');

// Tất cả routes đều require authentication
router.use(authenticateToken);

// ==================== EMOTIONS ====================
router.get('/emotions', gitJournalController.getAllEmotions);

// ==================== COMMITS ====================
router.post('/commits', gitJournalController.createCommit);
router.get('/commits', gitJournalController.getCommits);
router.get('/commits/:id', gitJournalController.getCommitById);

// ==================== SEVERITY ALERTS ====================
router.get('/alerts', gitJournalController.getAlerts);

// ==================== DAILY MERGE ====================
router.post('/merge', gitJournalController.createDailyMerge);
router.get('/merges', gitJournalController.getDailyMerges);
router.get('/merges/:date', gitJournalController.getDailyMergeByDate);

// ==================== GIT GRAPH ====================
router.get('/graph', gitJournalController.getGitGraphData);

// Test endpoint
router.get('/graph-test', (req, res) => {
    console.log('[GRAPH TEST] Endpoint hit!');
    res.json({
        success: true,
        message: 'Test endpoint working',
        user_id: req.user.user_id
    });
});

module.exports = router;
