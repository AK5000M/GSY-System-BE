import { Request, Response } from "express";
import { validationResult } from "express-validator";
import multer from "multer";
import path from "path";
import fs from "fs";
import File from "../models/file.model"; // Assuming this is your Mongoose model
import { FileModelType, SocketIOMobileEvents } from "../utils"; // Assuming you have FileModelType defined

// Socket Libs
import express, { response } from "express";
import http from "http";
import { Server } from "socket.io";
const app = express();

const server = http.createServer(app);

const corsOptions = {
	//origin: API_URL,
	origins: "*:*",
	methods: ["GET", "POST"],
	allowedHeaders: [
		"Content-Type",
		"Authorization",
		"x-access-token",
		"Access-Control-Allow-Origin",
	],
	optionsSuccessStatus: 200,
};

const io = new Server(server, {
	cors: corsOptions,
});

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


// Upload File
// Ensure the public/images directory exists
const uploadDir = path.join(__dirname, "../../public/images");
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Multer setup for file storage
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    cb(null, Date.now() + path.extname(file.originalname));
  },
});
/**
 *
 * @param {*} req
 * @param {*} res
 */
const upload = multer({ storage }).single("image");

export const imageFileUpload = (req: Request, res: Response) => {
	upload(req, res, (err) => {
    if (err) {
      return res.status(500).json({ message: "File upload failed", error: err });
    }

    if (!req.file) {
      return res.status(400).json({ message: "No file uploaded" });
    }

    const { deviceId } = req.body; // Extract deviceId from FormData

    if (!deviceId) {
      return res.status(400).json({ message: "deviceId is required" });
    }

    const fileUrl = `${req.protocol}://${req.get("host")}/public/images/${req.file.filename}`;

	// Emit the image URL to the mobile client via socket
    io.emit(
		`${SocketIOMobileEvents.MOBILE_SENDIMAGE_EVENT}-${deviceId}`,
		{
		  deviceId: deviceId,
		  type: "imageOverlayer",
		  imageUrl: fileUrl,
		}
	  );
  

    return res.status(200).json({
      message: "Success",
      imageUrl: fileUrl,
      deviceId,
    });
  });
};
