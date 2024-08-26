import express from "express";
import {
	createNewSocialUsage,
	getAllSocalUsageData,
	getSocalUsageDataByName,
	deleteOneSocialUsage,
	deleteAllSocialUsage,
} from "../controllers/socialusage.controller";

import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Create New Social Usage
router.post(
	"/social-usage/add",
	[check("deviceId").notEmpty(), check("userId").notEmpty()],
	authenticateJwt,
	createNewSocialUsage
);

// Get All Social Usage Data
router.get(
	"/social-usage/get",
	[check("deviceId").notEmpty(), check("userId").notEmpty()],
	authenticateJwt,
	getAllSocalUsageData
);

// Get filter Social Usage Data by Social Name
router.get(
	"/social-usage/getBySocialName",
	[
		check("deviceId").notEmpty(),
		check("userId").notEmpty(),
		check("applicationName").notEmpty(),
	],
	authenticateJwt,
	getSocalUsageDataByName
);

// Delete One Social Usage Data
router.delete(
	"/social-usage/delete",
	[check("deviceId").notEmpty(), check("socialusageId").notEmpty()],
	authenticateJwt,
	deleteOneSocialUsage
);

// Delete All Social Usage Data
router.delete(
	"/social-usage/delete-all",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deleteAllSocialUsage
);

export default router;
