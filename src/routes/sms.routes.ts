import express from "express";
import {
	getSMSList,
	getMessages,
	deleteMessage,
	deleteAllMessages,
} from "../controllers/sms.controller";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Get SMS List
router.get(
	"/smslist/get/:deviceId/",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	getSMSList
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
