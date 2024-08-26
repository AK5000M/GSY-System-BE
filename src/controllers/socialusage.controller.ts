import { Request, Response } from "express";
import { validationResult } from "express-validator";

import SocialUsage from "../models/socialusage.model";
import { SocialUsageModelType } from "../utils";

// create new social usage
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const createNewSocialUsage = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		// Extract data from the request body
		const {
			deviceId,
			userId,
			applicationName,
			appOpenedCount,
			totalTimeSpent,
		} = req.body;

		// Create a new social usage document
		const newSocialUsage: SocialUsageModelType = new SocialUsage({
			deviceId,
			userId,
			applicationName,
			appOpenedCount,
			totalTimeSpent,
			// Optionally, set other fields like created_at if needed
		});

		// Save the new social usage document to the database
		await newSocialUsage.save();

		// Send a success response
		res.status(201).json({
			success: true,
			message: "Social usage added successfully",
			socialUsage: newSocialUsage,
		});
	} catch (error) {
		console.error("Error add social usage:", error);
		res.status(500).json({
			error: "Failed to add social usage",
		});
	}
};

// get all data
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getAllSocalUsageData = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { deviceId, userId } = req.body;

		// Find all social usage data based on deviceId and userId
		const socialUsageData = await SocialUsage.find({ deviceId, userId });

		if (!socialUsageData || socialUsageData.length === 0) {
			return res
				.status(404)
				.json({ error: "Social usage data not found" });
		}

		// Return the social usage data
		res.status(200).json({ data: socialUsageData });
	} catch (error) {
		console.error("Error get social usage:", error);
		res.status(500).json({ error: "Failed to get social usage" });
	}
};

// get data by name
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getSocalUsageDataByName = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { deviceId, userId, applicationName } = req.body;

		// Find all social usage data based on deviceId and userId
		const socialUsageData = await SocialUsage.find({
			deviceId,
			userId,
			applicationName,
		});

		if (!socialUsageData || socialUsageData.length === 0) {
			return res
				.status(404)
				.json({ error: "Social usage data not found" });
		}

		// Return the social usage data
		res.status(200).json({ data: socialUsageData });
	} catch (error) {
		console.error("Error get social usage:", error);
		res.status(500).json({ error: "Failed to get social usage" });
	}
};

// delete one social usage
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteOneSocialUsage = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { deviceId, socialUsageId } = req.body;

		// Find and delete the social usage entry by deviceId and socialUsageId
		const deletedUsage = await SocialUsage.findOneAndDelete({
			deviceId,
			_id: socialUsageId,
		});

		if (!deletedUsage) {
			return res
				.status(404)
				.json({ error: "Social usage data not found" });
		}

		res.status(200).json({
			message: "Social usage data deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting social usage:", error);
		res.status(500).json({ error: "Failed to delete social usage data" });
	}
};

// delete all social usage
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteAllSocialUsage = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { deviceId } = req.body;

		// Find and delete the social usage entry by deviceId and socialUsageId
		const deletedUsage = await SocialUsage.findOneAndDelete({
			deviceId,
		});

		if (!deletedUsage) {
			return res
				.status(404)
				.json({ error: "Social usage data not found" });
		}

		res.status(200).json({
			message: "Social usage data deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting social usage:", error);
		res.status(500).json({ error: "Failed to delete social usage data" });
	}
};
