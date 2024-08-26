import express from "express";
import {
	addNewGallery,
	getGallery,
	deleteGallery,
	deleteAllGallery,
} from "../controllers/gallery.controller";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Add New Gallery
router.post(
	"/gallery/add",
	[
		check("deviceId").notEmpty(),
		check("name").notEmpty(),
		check("userId").notEmpty(),
	],
	authenticateJwt,
	addNewGallery
);

// Get Files
router.get(
	"/gallery/get",
	[check("deviceId").notEmpty(), check("userId").notEmpty()],
	authenticateJwt,
	getGallery
);

// Delete Gallery
router.delete(
	"/gallery/delete",
	[check("deviceId").notEmpty(), check("galleryId").notEmpty()],
	authenticateJwt,
	deleteGallery
);

// Delete All Gallery
router.delete(
	"/gallery/delete-all",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deleteAllGallery
);

export default router;
