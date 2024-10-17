import { Request, Response } from "express";
import { validationResult } from "express-validator";

import SMS from "../models/sms.models";
import { SMSModelType } from "../utils";

// Add New Messages
export const addNewMessage = async (data: any) => {
	try {
		// Extract device data from the request body
		const { deviceId, message, phonenumber } = data;

		const newMessage: SMSModelType = new SMS({
			deviceId,
			message,
			phonenumber,
			created_at: Date.now(),
			// Add other fields as needed
		});

		await newMessage.save();
	} catch (error) {
		console.error("Error adding message:", error);
		return {
			success: false,
			message: "Failed to add message",
			error: error,
		};
	}
};

// Get SMS List
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const getSMSList = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.params.deviceId;

		// Aggregate query to get the last message per phone number
		const messages = await SMS.aggregate([
			{
				$match: { deviceId: deviceId },
			},
			{
				$sort: { created_at: -1 },
			},
			{
				$group: {
					_id: "$phonenumber",
					deviceId: { $first: "$deviceId" },
					lastMessage: { $first: "$message" },
					phoneNumber: { $first: "$phonenumber" },
					createdAt: { $first: "$created_at" },
				},
			},
			{
				$sort: { createdAt: -1 },
			},
		]);

		// Check if messages are found
		if (messages.length === 0) {
			return res.status(404).json({ error: "Messages not found" });
		}
		// Return the grouped messages in the response
		res.status(200).json(messages);
	} catch (error) {
		console.error("Error fetching messages:", error);
		res.status(500).json({ error: "Failed to fetch messages" });
	}
};

// Get All SMS
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const getAllSMS = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { deviceId, phoneNumber } = req.params;

		// Find all SMS messages by deviceId and phoneNumber
		const allSms: SMSModelType[] = await SMS.find({
			deviceId: deviceId,
			phonenumber: phoneNumber,
		}).sort({ created_at: -1 });

		// Check if any SMS messages were found
		if (!allSms || allSms.length === 0) {
			return res.status(404).json({
				success: false,
				error: "No messages found for this device and phone number.",
			});
		}

		// Return the retrieved SMS messages
		res.status(200).json({ success: true, allSms });
	} catch (error) {
		console.error("Error fetching messages:", error);
		res.status(500).json({
			success: false,
			error: "Failed to fetch messages",
		});
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
