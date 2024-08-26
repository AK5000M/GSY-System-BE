// src/routes/app.routes.js
import express from "express";
import multer from "multer";
import path from "path";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";
import { createNewApk, getNewApk } from "../controllers/app.controller";

// Set up multer for file uploads
const storage = multer.diskStorage({
	destination: (req, file, cb) => {
		cb(null, path.join(__dirname, "../../public/uploads/")); // specify your uploads directory
	},
	filename: (req, file, cb) => {
		cb(
			null,
			file.fieldname + "-" + Date.now() + path.extname(file.originalname)
		);
	},
});

const upload = multer({ storage: storage });

const router = express.Router();

// Create new app with userId and file upload
router.post(
	"/app/new/create",
	upload.single("appIcon"), // Handle file upload with the key 'appIcon'
	[check("userId").notEmpty(), check("appName").notEmpty()],
	authenticateJwt,
	createNewApk
);

// Get new apk
router.get(
	"/app/get-apk/:data",
	[check("data").notEmpty()],
	authenticateJwt,
	getNewApk
);

export default router;
