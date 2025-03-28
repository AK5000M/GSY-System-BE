import { Request, Response } from "express";
import { validationResult } from "express-validator";
import File from "../models/file.model"; // Assuming this is your Mongoose model
import { FileModelType } from "../utils"; // Assuming you have FileModelType defined
// New Files
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const sendNewFile = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { deviceId, files, type, size, sender, receiver } = req.body;

		// Create a new file document using Mongoose model
		const newFile: FileModelType = new File({
			deviceId,
			files, // Assuming `files` contains the file URL or path
			type,
			size,
			sender,
			receiver,
		});

		// Save the new file document to the database
		await newFile.save();

		res.status(201).json({
			success: true,
			message: "File saved successfully",
			file: newFile,
		});
	} catch (error) {
		console.error("Error uploading file:", error);
		res.status(500).json({ error: "Failed to save file to the database" });
	}
};

// Get Files
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getFiles = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { deviceId, sender } = req.body;

		// Find files in the database that match the query
		const files: FileModelType[] = await File.find({
			deviceId: deviceId,
			receiver: sender,
		});

		res.status(200).json({ success: true, files });
	} catch (error) {
		console.error("Error fetching files:", error);
		res.status(500).json({ error: "Failed to fetch files" });
	}
};

// Delete File
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteFile = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { deviceId, fileId } = req.body;

		// Delete file in the database that match the query
		const result = await File.deleteOne({
			_id: fileId,
			deviceId: deviceId,
		});

		// Check if a messages was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "File not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "File deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting file:", error);
		res.status(500).json({ error: "Failed to delete file" });
	}
};

// Delete All Files
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteAllFiles = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.body.deviceId;

		// Delete files in the database that match the query
		const result = await File.deleteMany({
			deviceId: deviceId,
		});

		// Check if a files was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "Files not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "All Files deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting files:", error);
		res.status(500).json({ error: "Failed to delete files" });
	}
};


