import express from "express";
import {
	addNewMessage,
	getMessages,
	deleteMessage,
	deleteAllMessages,
} from "../controllers/sms.controller";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Add New Messages
router.post(
	"/sms/add",
	[check("deviceId").notEmpty(), check("sender").notEmpty()],
	authenticateJwt,
	addNewMessage
);

// Get Messages
router.get(
	"/sms/get",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	getMessages
);

// Delete Message
router.delete(
	"/sms/delete",
	[check("deviceId").notEmpty(), check("messageId").notEmpty()],
	authenticateJwt,
	deleteMessage
);

// Delete All Messages of Device
router.delete(
	"/sms/delete-all",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deleteAllMessages
);

export default router;
