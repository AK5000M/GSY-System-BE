import { Request, Response } from "express";
import { validationResult } from "express-validator";

import Calls from "../models/call.model";
import { CallsModelType } from "../utils";

// Add New Calls
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const addNewCalls = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const {
			deviceId,
			phoneNumber,
			callStatus,
			callTime,
			// Add other fields as needed
		} = req.body;

		const newCalls: CallsModelType = new Calls({
			deviceId,
			phoneNumber,
			callStatus,
			callTime,
			// Add other fields as needed
		});

		await newCalls.save();

		// ----- File Upload function --------

		// Return a success response
		res.status(201).json({
			success: true,
			message: "Calls added successfully",
		});
	} catch (error) {
		console.error("Error adding calls:", error);
		res.status(500).json({ error: "Failed to add calls" });
	}
};

// Call History Save
export const addNewCallHistory = async (data: any) => {
	try {
		const {
			deviceId,
			phoneNumber,
			callStatus,
			callTime,
			// Add other fields as needed
		} = data;

		const newCalls: CallsModelType = new Calls({
			deviceId,
			phoneNumber,
			callStatus,
			callTime,
			// Add other fields as needed
		});

		await newCalls.save();

		// Return a success response
		return { status: 200, message: "Call history added successfully" };
	} catch (error) {
		console.error("Error adding calls:", error);
		return { status: 500, error: "Failed to add call" };
	}
};

// Get Calls History
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getAllCalls = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.params.deviceId; // Get deviceId from request body

		// Find calls in the database that match the query
		const calls: CallsModelType[] = await Calls.find({
			deviceId: deviceId,
		});

		// Check if calls are found
		if (calls.length === 0) {
			return res.status(404).json({ error: "Calls not found" });
		}

		// Return devices in the response
		res.status(200).json(calls);
	} catch (error) {
		console.error("Error fetching calls:", error);
		res.status(500).json({ error: "Failed to fetch calls" });
	}
};

// Get Calls by CallingPerson
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const getCallsByCallingPerson = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.body.deviceId; // Get deviceId from request body
		const callingPerson = req.body.callingPerson; // Get callingPerson from request body

		// Find calls in the database that match the query
		const calls: CallsModelType[] = await Calls.find({
			deviceId: deviceId,
			callingPerson: callingPerson,
		});

		// Check if calls are found
		if (calls.length === 0) {
			return res.status(404).json({ error: "Calls not found" });
		}

		// Return devices in the response
		res.status(200).json(calls);
	} catch (error) {
		console.error("Error fetching calls:", error);
		res.status(500).json({ error: "Failed to fetch calls" });
	}
};

// Delete Calls History by Device and CallingPerson, ID
/**
 *
 * @param {*} req
 * @param {*} res
 */

export const deleteCallingHostoryByCallingPerson = async (
	req: Request,
	res: Response
) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const callingId = req.body.callingId;
		const deviceId = req.body.deviceId;
		const callingPerson = req.body.callingPerson;

		// Delete calls in the database that match the query
		const result = await Calls.deleteOne({
			_id: callingId,
			deviceId: deviceId,
			callingPerson: callingPerson,
		});

		// Check if a calls was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "Calls not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "Calls deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting calls:", error);
		res.status(500).json({ error: "Failed to delete calls" });
	}
};

// Delete Calls by DeviceID
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteAllCalls = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.body.deviceId;

		// Delete calls in the database that match the query
		const result = await Calls.deleteMany({
			deviceId: deviceId,
		});

		// Check if a calls was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "calls not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "All calls deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting calls:", error);
		res.status(500).json({ error: "Failed to delete calls" });
	}
};
