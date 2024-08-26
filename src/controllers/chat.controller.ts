import { Request, Response } from "express";
import { validationResult } from "express-validator";

import Feedback from "../models/feedback";
import Chat from "../models/chat.model";
import { FeedbackModelType, MessageModelType } from "../utils";

// Chat Feedback
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const chatFeedback = async (req: Request, res: Response) => {
	try {
		const { user, feedback, rate } = req.body;

		// Check if all required fields are provided
		if (!user || !feedback || !rate) {
			return res.status(400).json({
				status: 400,
				message: "User, feedback, and rate are required.",
			});
		}
		// Create a new feedback document
		const newFeedback: FeedbackModelType = new Feedback({
			userId: user,
			feedback,
			rate,
		});

		// Save the feedback to the database
		await newFeedback.save();

		// Respond with a success message
		res.status(200).json({
			status: 200,
			message: "Feedback submitted successfully.",
		});
	} catch (error) {
		console.error("Feedback submission error:", error);
		res.status(500).json({ status: 500, message: "Internal server error" });
	}
};

// Chat Message
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const chatMessage = async (req: Request, res: Response) => {
	try {
		const { userId, message, sender } = req.body;

		// Check if all required fields are provided
		if (!userId || !message || !sender) {
			return res.status(400).json({
				status: 400,
				message: "User, message, and sender are required.",
			});
		}
		// Create a new message document
		const newMessage: MessageModelType = new Chat({
			userId,
			message,
			sender,
		});

		// Save the message to the database
		await newMessage.save();

		// Respond with a success message
		res.status(200).json({
			status: 200,
			message: "Message submitted successfully.",
		});
	} catch (error) {
		console.error("Message submission error:", error);
		res.status(500).json({ status: 500, message: "Internal server error" });
	}
};
