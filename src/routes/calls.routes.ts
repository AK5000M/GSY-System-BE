import express from "express";
import {
	addNewCalls,
	getAllCalls,
	getCallsByCallingPerson,
	deleteCallingHostoryByCallingPerson,
	deleteAllCalls,
} from "../controllers/calls.controller";

import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Add New Messages
router.post(
	"/calls/add",
	[check("deviceId").notEmpty(), check("callingPerson").notEmpty()],
	authenticateJwt,
	addNewCalls
);

// Get Messages by Device
router.get(
	"/calls/getAll/:deviceId",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	getAllCalls
);

// Get Messages by Device and CallingPerson
router.get(
	"/calls/getByCallingPerson",
	[check("deviceId").notEmpty(), check("callingPerson").notEmpty()],
	authenticateJwt,
	getCallsByCallingPerson
);

// Delete Calls History by Device and CallingPerson, ID
router.delete(
	"/calls/select-delete",
	[
		check("callingId").notEmpty(),
		check("deviceId").notEmpty(),
		check("callingPerson").notEmpty(),
	],
	authenticateJwt,
	deleteCallingHostoryByCallingPerson
);

// Delete Calls by DeviceID
router.delete(
	"/sms/delete-all",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deleteAllCalls
);

export default router;
