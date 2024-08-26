import express from "express";
import { chatFeedback, chatMessage } from "../controllers/chat.controller";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// feedback route
router.post(
	"/chat/feedback/send",
	[
		check("feedback").notEmpty(),
		check("rate").notEmpty(),
		check("user").notEmpty(),
	],
	authenticateJwt,
	chatFeedback
);

router.post(
	"/chat/message/send",
	[
		check("user").notEmpty(),
		check("message").notEmpty(),
		check("sender").notEmpty(),
	],
	authenticateJwt,
	chatMessage
);

export default router;
