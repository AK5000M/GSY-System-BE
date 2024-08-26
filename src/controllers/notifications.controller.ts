import { Request, Response } from "express";
import { validationResult } from "express-validator";

import Notification from "../models/notification.model";
import { NotificationsModelType } from "../utils";

// Add New Notifications
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const addNewNotifications = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		// Extract notifications data from the request body
		const {
			deviceId,
			title,
			content,
			sort,
			userId,
			// Add other fields as needed
		} = req.body;

		const newNotifications: NotificationsModelType = new Notification({
			deviceId,
			title,
			content,
			sort,
			userId,
			// Add other fields as needed
		});

		await newNotifications.save();

		// Return a success response
		res.status(201).json({
			success: true,
			message: "New Notification added successfully",
		});
	} catch (error) {
		console.error("Error adding notifications:", error);
		res.status(500).json({ error: "Failed to add notifications" });
	}
};

//Get All Notifications
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getAllNotifications = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const deviceId = req.body.deviceId;

		// Find notifications in the database that match the query
		const notifications: NotificationsModelType[] = await Notification.find(
			{
				deviceId: deviceId,
			}
		);

		// Check if notifications are found
		if (notifications.length === 0) {
			return res.status(404).json({ error: "Notifications not found" });
		}

		// Return notifications in the response
		res.status(200).json(notifications);
	} catch (error) {
		console.error("Error get notifications:", error);
		res.status(500).json({ error: "Failed to get notifications" });
	}
};

//Get Unread Notifications
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getUnreadNotifications = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { deviceId, active } = req.body;

		// Find notifications in the database that match the query, active value is false
		const unreadNotifications: NotificationsModelType[] =
			await Notification.find({
				deviceId: deviceId,
				active: active,
			});

		// Check if notifications are found
		if (unreadNotifications.length === 0) {
			return res.status(404).json({ error: "Notifications not found" });
		}

		// Return notifications in the response
		res.status(200).json(unreadNotifications);
	} catch (error) {
		console.error("Error get notifications:", error);
		res.status(500).json({ error: "Failed to get notifications" });
	}
};

// Active(Read) Notifications
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const activeNotifications = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { deviceId, notificationId } = req.body;
		// Update the notification in the database to set active to true
		const updateNotification = await Notification.updateOne(
			{ _id: notificationId, deviceId: deviceId, active: false },
			{ $set: { active: true } }
		);

		// Check if the notification was found and updated
		if (updateNotification.modifiedCount > 0) {
			return res.status(200).json({
				res: true,
				message: "Notification activated successfully",
			});
		} else {
			return res.status(404).json({
				res: false,
				error: "Notification not found or not updated",
			});
		}
	} catch (error) {
		console.error("Error activating notification:", error);
		res.status(500).json({ error: "Failed to activate notification" });
	}
};

// Delete One Notification
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const deleteOneNotification = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { deviceId, notificationId } = req.body;

		// Delete the notification from the database
		const deleteResult = await Notification.deleteOne({
			_id: notificationId,
			deviceId: deviceId,
		});

		// Check if the notification was found and deleted
		if (deleteResult.deletedCount && deleteResult.deletedCount > 0) {
			return res
				.status(200)
				.json({ message: "Notification deleted successfully" });
		} else {
			return res
				.status(404)
				.json({ error: "Notification not found or not deleted" });
		}
	} catch (error) {
		console.error("Error deleting notification:", error);
		res.status(500).json({ error: "Failed to delete notification" });
	}
};

// Delete All  Notifications
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const deleteAllNotification = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { deviceId } = req.body;

		// Delete the notification from the database
		const deleteResult = await Notification.deleteOne({
			deviceId: deviceId,
		});

		// Check if the notification was found and deleted
		if (deleteResult.deletedCount && deleteResult.deletedCount > 0) {
			return res
				.status(200)
				.json({ message: "Notifications deleted successfully" });
		} else {
			return res
				.status(404)
				.json({ error: "Notifications not found or not deleted" });
		}
	} catch (error) {
		console.error("Error deleting notifications:", error);
		res.status(500).json({ error: "Failed to delete notifications" });
	}
};
