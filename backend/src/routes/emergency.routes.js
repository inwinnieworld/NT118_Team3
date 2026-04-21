const express = require("express");
const controller = require("../controllers/emergency.controller");

const router = express.Router();

router.get("/resources", controller.getResources);
router.get("/contacts/:studentId", controller.getContactsByStudentId);
router.post("/contacts", controller.createContact);
router.put("/contacts/:contactId", controller.updateContact);
router.delete("/contacts/:contactId", controller.deleteContact);

module.exports = router;