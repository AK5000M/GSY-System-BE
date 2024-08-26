import express from "express";
import {
	addNewNotifications,
	getAllNotifications,
	getUnreadNotifications,
	activeNotifications,
	deleteOneNotification,
	deleteAllNotification,
} from "../controllers/notifications.controller";

import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Create New Notification
router.post(
	"/notifications/create",
	[
		check("deviceId").notEmpty(),
		check("content").notEmpty(),
		check("userId").notEmpty(),
	],
	authenticateJwt,
	addNewNotifications
);

// Get All Notifications
router.get(
	"/notifications/whole",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	getAllNotifications
);

// Get Unread Notifications
router.get(
	"/notifications/unread-notifications",
	[check("deviceId").notEmpty(), check("active").isBoolean()],
	authenticateJwt,
	getUnreadNotifications
);

// Active(Read) Notifications
router.post(
	"/notifications/read-notification",
	[check("deviceId").notEmpty(), check("notificationId").notEmpty()],
	authenticateJwt,
	activeNotifications
);

// Delete Notification
router.delete(
	"/notifications/delete",
	[check("deviceId").notEmpty(), check("notificationId").notEmpty()],
	authenticateJwt,
	deleteOneNotification
);

// Delete All Notifications
router.delete(
	"/notifications/all-delete",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deleteAllNotification
);

export default router;
