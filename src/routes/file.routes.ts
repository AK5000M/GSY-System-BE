import express from "express";
import {
	sendNewFile,
	getFiles,
	deleteFile,
	deleteAllFiles,
} from "../controllers/file.controller";

import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// send files
router.post(
	"/file/send",
	[
		check("deviceId").notEmpty(),
		check("files").notEmpty(),
		check("sender").notEmpty(),
		check("reciver").notEmpty(),
	],
	authenticateJwt,
	sendNewFile
);

// get files
router.get(
	"/file/get",
	[check("deviceId").notEmpty(), check("sender").notEmpty()],
	authenticateJwt,
	getFiles
);

// delete file
router.delete(
	"/file/delete",
	[check("deviceId").notEmpty(), check("fileId").notEmpty()],
	authenticateJwt,
	deleteFile
);

// Delete All Files of Device
router.delete(
	"/file/delete-all",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deleteAllFiles
);

export default router;
