import express from "express";
import {
	addDevice,
	getDevice,
	DeleteDevice,
	getDeviceInfo,
	getAllDevices,
	updateDeviceName,
	getDevicePassword,
} from "../controllers/device.controller";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";
import { deviceRateLimiter, userRateLimiter } from "../modules/limitrate";

const router = express.Router();

// Add New Device
router.post(
	"/device/add",
	[
		check("deviceId").notEmpty(),
		check("deviceInfo").notEmpty(),
		check("hwid").notEmpty(),
		check("installationDate").notEmpty(),
		check("manufacturer").notEmpty(),
		check("models").notEmpty(),
		check("version").notEmpty(),
	],
	authenticateJwt,
	addDevice
);

//Get A Device by deviceID
router.get(
	"/device/getInfo/:deviceId",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deviceRateLimiter,
	getDeviceInfo
);

// Get All Devices
router.get(
	"/device/get/:userId",
	[check("userId").notEmpty()],
	authenticateJwt,
	userRateLimiter,
	getDevice
);

// Update Device Name
router.post(
	"/device/update/deviceName/",
	[check("deviceId").notEmpty(), check("editedManufacturer").notEmpty()],
	authenticateJwt,
	deviceRateLimiter,
	updateDeviceName
);

// Get Device Password
router.get(
	"/device/get/password/:deviceId",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deviceRateLimiter,
	getDevicePassword
);

// Delete a device
router.delete(
	"/device/delete/:deviceId",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	// deviceRateLimiter,
	DeleteDevice
);

// Admin get all devices
router.get("/admin/device/get-all-devices", authenticateJwt, getAllDevices);

export default router;
