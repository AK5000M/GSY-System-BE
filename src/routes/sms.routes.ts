import express from "express";
import {
	getSMSList,
	getAllSMS,
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

// Get All SMS
router.get(
	"/sms/getAll/:deviceId/:phoneNumber",
	[check("phoneNumber").notEmpty()],
	authenticateJwt,
	getAllSMS
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
