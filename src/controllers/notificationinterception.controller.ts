import { Request, Response } from "express";
import { validationResult } from "express-validator";

import NotificationInterception from "../models/notificationinterception.model";
import { NotificationInterceptionType } from "../utils";

// Enable or Disable the Notifications
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const controlNotification = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
	} catch (error) {
		console.error("Error set notifications interception:", error);
		res.status(500).json({
			error: "Failed to set notifications interception",
		});
	}
};
