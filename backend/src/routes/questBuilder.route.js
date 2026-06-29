const express = require('express');
const router = express.Router();
const authMiddleware = require('../middlewares/auth.middleware');
const adminMiddleware = require('../middlewares/admin.middleware');
const staffOrAdminMiddleware = require('../middlewares/staffOrAdmin.middleware');
const controller = require('../controllers/questBuilder.controller');
const questMediaUpload = require('../middlewares/questMediaUpload.middleware');

router.use(authMiddleware);

router.get('/engines', controller.getEngineCatalog);
router.post('/media', staffOrAdminMiddleware, questMediaUpload.single('media'), controller.uploadQuestMedia);
router.get('/categories', staffOrAdminMiddleware, controller.getQuestCategories);
router.get('/recommendations', controller.recommendQuests);
router.get('/catalog', controller.listApprovedQuestCatalog);

router.get('/quests', staffOrAdminMiddleware, controller.listQuests);
router.post('/quests/draft', staffOrAdminMiddleware, controller.saveQuestDraft);
router.post('/quests/:questId/submit-review', staffOrAdminMiddleware, controller.submitQuestForReview);
router.delete('/quests/:questId/draft', staffOrAdminMiddleware, controller.deleteOwnDraftQuest);
router.post('/quests/:questId/review', adminMiddleware, controller.reviewQuest);
router.post('/quests/:questId/visibility', adminMiddleware, controller.updateQuestVisibility);
router.get('/versions/:versionId', staffOrAdminMiddleware, controller.getQuestVersion);
router.get('/reports/monthly', staffOrAdminMiddleware, controller.getOwnQuestMonthlyReport);
router.get('/reports/ranking', staffOrAdminMiddleware, controller.getOwnQuestRankingReport);
router.get('/reports/events', staffOrAdminMiddleware, controller.getOwnQuestEventReport);

router.get('/quests/:questId/approved-flow', controller.getApprovedQuestFlow);
router.post('/runs/start', controller.startQuestRun);
router.post('/runs/:runId/events', controller.appendQuestRunEvent);
router.post('/runs/:runId/finish', controller.finishQuestRun);

module.exports = router;
