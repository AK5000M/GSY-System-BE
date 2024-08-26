import express from "express";
import { controlNotification } from "../controllers/notificationinterception.controller";

import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Create New Notification
router.post(
	"/notifications-interception/control",
	[check("deviceId").notEmpty(), check("userId").notEmpty()],
	authenticateJwt,
	controlNotification
);

export default router;
