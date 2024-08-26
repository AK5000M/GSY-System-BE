import { Request, Response } from "express";
import { validationResult } from "express-validator";

import SMS from "../models/sms.models";
import { SMSModelType } from "../utils";

// Add New Messages
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const addNewMessage = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		// Extract device data from the request body
		const {
			deviceId,
			messages,
			sender,
			receiver,
			// Add other fields as needed
		} = req.body;

		const newMessage: SMSModelType = new SMS({
			deviceId,
			messages,
			sender,
			receiver,
			// Add other fields as needed
		});

		await newMessage.save();

		// Return a success response
		res.status(201).json({
			success: true,
			message: "Message added successfully",
		});
	} catch (error) {
		console.error("Error adding message:", error);
		res.status(500).json({ error: "Failed to add message" });
	}
};

// Get Messages
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const getMessages = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.body.deviceId;

		// Find messages in the database that match the query
		const messages: SMSModelType[] = await SMS.find({
			deviceId: deviceId,
		});

		// Check if messages are found
		if (messages.length === 0) {
			return res.status(404).json({ error: "Messages not found" });
		}

		// Return messages in the response
		res.status(200).json(messages);
	} catch (error) {
		console.error("Error fetching messages:", error);
		res.status(500).json({ error: "Failed to fetch messages" });
	}
};

// Delete Message
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const deleteMessage = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const messageId = req.body.messageId;
		const deviceId = req.body.deviceId;

		// Delete messages in the database that match the query
		const result = await SMS.deleteOne({
			_id: messageId,
			deviceId: deviceId,
		});

		// Check if a messages was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "Message not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "Message deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting message:", error);
		res.status(500).json({ error: "Failed to delete message" });
	}
};

// Delete All Messages

export const deleteAllMessages = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.body.deviceId;

		// Delete messages in the database that match the query
		const result = await SMS.deleteMany({
			deviceId: deviceId,
		});

		// Check if a messages was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "Message not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "All Messages deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting message:", error);
		res.status(500).json({ error: "Failed to delete message" });
	}
};
