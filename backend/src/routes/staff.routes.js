const express = require("express");
const controller = require("../controllers/staff.controller");

const router = express.Router();

router.post("/quests", controller.createQuest);
router.put("/quests/:questId", controller.updateQuest);
router.get("/quests", controller.getAllQuests);

router.post("/assignments", controller.assignQuestToStudent);
router.get("/assignments", controller.getQuestAssignments);

router.get("/reports/summary", controller.getSummaryReport);
router.get("/reports/errors", controller.getErrorReport);
router.get("/reports/quests", controller.getQuestReport);

module.exports = router;