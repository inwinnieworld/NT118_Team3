const express = require("express");
const router = express.Router();
const authController = require("../controllers/auth.controller");

router.post("/register", authController.register);
router.post("/login", authController.login);

router.post("/forgot-password", authController.forgotPasswordRequest);
router.post("/validate-reset-token", authController.validateResetToken);
router.post("/reset-password", authController.resetPassword);

router.get("/open-reset-password", authController.openResetPasswordPage);

module.exports = router;