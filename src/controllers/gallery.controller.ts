import { Request, Response } from "express";
import { validationResult } from "express-validator";

import Gallery from "../models/gallery.model";
import { GalleryModelType } from "../utils";

// Add New Gallery
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const addNewGallery = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { Id, deviceId, filepath } = req.body;

		// Create a new file document using Mongoose model
		const newGallery: GalleryModelType = new Gallery({
			Id,
			deviceId,
			filepath,
		});

		// Save the new gallery document to the database
		await newGallery.save();

		res.status(201).json({
			success: true,
			message: "Gallery saved successfully",
			file: newGallery,
		});
	} catch (error) {
		console.error("Error adding gallery:", error);
		res.status(500).json({ error: "Failed to add gallery" });
	}
};

// Get Gallery
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getGallery = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { deviceId, userId } = req.body;
		// // Find gallery in the database that match the query
		const gallery: GalleryModelType[] = await Gallery.find({
			deviceId: deviceId,
			userId: userId,
		});
		res.status(200).json({ success: true, gallery });
	} catch (error) {
		console.error("Error fetching gallery:", error);
		res.status(500).json({ error: "Failed to fetch gallery" });
	}
};

// Delete Gallery
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteGallery = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { deviceId, galleryId } = req.body;

		// Delete gallery in the database that match the query
		const result = await Gallery.deleteOne({
			_id: galleryId,
			deviceId: deviceId,
		});

		// Check if a gallery was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "Gallery not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "Gallery deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting Gallery:", error);
		res.status(500).json({ error: "Failed to Gallery file" });
	}
};

// Delete All Gallery
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteAllGallery = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.body.deviceId;

		// Delete galleries in the database that match the query
		const result = await Gallery.deleteMany({
			deviceId: deviceId,
		});

		// Check if a galleries was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "galleries not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "All Galleries deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting galleries:", error);
		res.status(500).json({ error: "Failed to delete galleries" });
	}
};
