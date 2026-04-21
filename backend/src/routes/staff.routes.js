const express = require("express");
const controller = require("../controllers/staff.controller");

const router = express.Router();

router.post("/quests", controller.createQuest);
router.put("/quests/:questId", controller.updateQuest);
router.get("/quests", controller.getAllQuests);
router.delete("/quests/:questId", controller.deleteQuest);

router.post("/assignments", controller.assignQuestToStudent);
router.get("/assignments", controller.getQuestAssignments);

router.get("/reports/summary", controller.getSummaryReport);
router.get("/reports/errors", controller.getErrorReport);
router.get("/reports/quests", controller.getQuestReport);
router.get("/reports/quest-trend", controller.getQuestTrendReport);
router.get("/reports/quest-monthly-metrics", controller.getQuestMonthlyMetrics);
router.get("/reports/quest-ranking-board", controller.getQuestRankingBoard);


router.get("/trace-questions", controller.getAllTraceQuestions);
router.post("/trace-questions", controller.createTraceQuestion);
router.get("/trace-questions/:questionId", controller.getTraceQuestionDetail);
router.put("/trace-questions/:questionId", controller.updateTraceQuestion);
router.delete("/trace-questions/:questionId", controller.deleteTraceQuestion);

module.exports = router;