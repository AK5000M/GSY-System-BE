import { Request, Response } from "express";
import { validationResult } from "express-validator";

import Device from "../models/device.model";
import User from "../models/user.model";
import { DeviceModelType, UserModelType } from "../utils";

// Add New Device from QR code capture via WS
export const addNewDeviceInfo = async (device: DeviceModelType) => {
	try {
		// Initial data of a Device
		const {
			deviceId,
			userId,
			deviceInfo,
			hwid, //  Hardware ID
			installationDate, // Apk install date
			manufacturer, // Device Info
			models, // Device Model
			version,
			userType,
		} = device;

		// Check if device with deviceId already exists
		const existDevice: DeviceModelType | null = await Device.findOne({
			deviceId,
		});

		if (existDevice) {
			return {
				success: false,
				message: "Device with this deviceId already exists",
			};
		}

		const userData: UserModelType | any = await User.findById(userId);

		if (
			userData?.subscribe == "basic" &&
			userData?.status == "allowed" &&
			userData.devices >= 1
		) {
			return {
				success: false,
				message: "User already has devices, cannot add more",
			};
		}

		// Create a new Device document using Mongoose model
		const newDevice: DeviceModelType = new Device({
			deviceId,
			userId,
			deviceInfo,
			hwid,
			installationDate,
			manufacturer,
			models,
			online: true,
			version,
			userType,
		});

		// Save the new device to the database
		const successSave = await newDevice.save();

		if (successSave) {
			// Increment the user's devices count by 1
			await User.findByIdAndUpdate(userId, { $inc: { devices: 1 } });
			return {
				success: true,
				message: "Device added successfully",
			};
		}
	} catch (error) {
		console.error("Error adding device:", error);
	}
};

// Online setting of a Device via WS
export const setOnlineDevice = async (deviceStatus: any) => {
	try {
		// Extract device information from deviceStatus
		const { deviceId } = deviceStatus;

		// Update the online status of the device only if it's not already online
		const updatedUser = await Device.updateOne(
			{ deviceId: deviceId },
			{
				$set: {
					online: true,
				},
			}
		);
		if (updatedUser)
			return {
				success: true,
				message: "online set successfully",
			};
	} catch (error) {
		console.error("Error online setting:", error);
	}
};

// Offline setting of a Device via WS
export const setOfflineDevice = async (deviceStatus: any) => {
	try {
		const { deviceId } = deviceStatus;

		// Check if the device exists
		const existingDevice = await Device.findOne({ deviceId: deviceId });
		if (!existingDevice) {
			console.log("Device not found in the database.");
			return;
		}

		// Check if the device is already offline
		if (!existingDevice.online) {
			console.log("Device is already offline. Skipping update.");
			return;
		}

		// Update the online status of the device only if it's not already online
		const updatedUser = await Device.updateOne(
			{ deviceId: deviceId },
			{
				$set: {
					online: false,
				},
			}
		);
		if (updatedUser)
			return {
				success: true,
				message: "offline set successfully",
			};
	} catch (error) {
		console.error("Error offline setting:", error);
	}
};

// Update Battery and Network
export const updateBatteryAndNetwork = async (data: any) => {
	try {
		console.log("device info:", data);
		const { deviceId, batteryStatus, connectStatus } = data;
		const updatedDevice = await Device.updateOne(
			{ deviceId: deviceId },
			{
				$set: {
					batteryStatus: batteryStatus,
					connectStatus: connectStatus,
				},
			}
		);

		if (updatedDevice)
			return {
				success: true,
				message: "device update successfully",
			};
	} catch (error) {
		console.log("update battery and network error:", error);
	}
};

// Update Black and Lock Screen
export const updateBlackAndLock = async (data: any) => {
	try {
		const { type, deviceId, status } = data;
		if (type == "blackScreen") {
			const updatedDevice = await Device.updateOne(
				{ deviceId: deviceId },
				{
					$set: {
						blackScreen: status,
					},
				}
			);
			if (updatedDevice)
				return {
					success: true,
					message: "device update successfully",
				};
		} else if (type == "lockScreen") {
			const updatedDevice = await Device.updateOne(
				{ deviceId: deviceId },
				{
					$set: {
						lockScreen: status,
					},
				}
			);
			if (updatedDevice)
				return {
					success: true,
					message: "device update successfully",
				};
		}
	} catch (error) {
		console.log("update black and lock error:", error);
	}
};

// Add New Device via API
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const addDevice = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		// Initial data of a Device
		const {
			deviceId,
			deviceInfo,
			hwid, //  Hardware ID
			installationDate, // Apk install date
			manufacturer, // Device Info
			models, // Device Model
			version,
		} = req.body;

		// Extract userId from the authenticated request
		const user = req.user as UserModelType; // Assuming the user ID is stored in req.user.id
		const userId = user?._id;

		// Check if device with deviceId already exists
		const existDevice: DeviceModelType | null = await Device.findOne({
			deviceId,
		});
		if (existDevice) {
			return res
				.status(400)
				.json({ error: "Device with this deviceId already exists" });
		}

		// Create a new Device document using Mongoose model
		const newDevice: DeviceModelType = new Device({
			deviceId,
			userId,
			deviceInfo,
			hwid,
			installationDate,
			manufacturer,
			models,
			version,
		});

		// Save the new device to the database
		await newDevice.save();

		// Return a success response
		res.status(201).json({
			success: true,
			message: "Device added successfully",
		});
	} catch (error) {
		console.error("Error adding device:", error);
		res.status(500).json({ error: "Failed to add device" });
	}
};

// Device Info Update
/**
 *
 * @param {*} req
 * @param {*} res
 */
// After Device, Update the device Info

// Get Devices by UserId
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getDevice = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const userId = req.params.userId; // Get userId from request body
		// Find devices in the database that match the query
		const devices: DeviceModelType[] = await Device.find({
			userId: userId,
		}).sort({ created_at: -1 });

		// Return devices in the response
		res.status(200).json(devices);
	} catch (error) {
		console.error("Error fetching devices:", error);
		res.status(500).json({ error: "Failed to fetch devices" });
	}
};

// Get all devices for Admin
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getAllDevices = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		// Find devices in the database that match the query
		const devices: DeviceModelType[] = await Device.find().sort({
			created_at: -1,
		});

		// Return devices in the response
		res.status(200).json(devices);
	} catch (error) {
		console.error("Error fetching devices:", error);
		res.status(500).json({ error: "Failed to fetch devices" });
	}
};

//Get Device Info by Device ID
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getDeviceInfo = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const deviceId = req.params.deviceId;

		//Find device in the database that match the query by deviceId
		const device: DeviceModelType[] = await Device.find({
			_id: deviceId,
		});

		// Check if devices are found
		if (device.length > 0) {
			res.status(200).json(device);
		} else {
			return res.status(404).json({ error: "Device not found" });
		}
	} catch (error) {
		console.log("error");
	}
};

// Update Device Name
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const updateDeviceName = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { deviceId, editedManufacturer } = req.body;
	try {
		const device: DeviceModelType | null = await Device.findOne({
			deviceId: deviceId,
		});

		if (!device) {
			return res
				.status(404)
				.json({ status: 404, message: "Device not found" });
		}

		device.manufacturer = editedManufacturer;
		const updateDevice = await device.save();

		// Send back the updated device information as a response
		res.status(200).json({
			status: 200,
			message: "Device manufacturer updated successfully",
			device: updateDevice,
		});
	} catch (error) {
		console.error("Error processing device name update:", error);
		res.status(500).json({
			status: 500,
			error: "Failed to process the device name update",
		});
	}
};

// Delete Device
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const DeleteDevice = async (req: Request, res: Response) => {
	try {
		const deviceId = req.params.deviceId;

		const device = await Device.findOne({ deviceId: deviceId });

		if (!device) {
			return res
				.status(404)
				.json({ error: "Device not found or already deleted" });
		}

		const userId = device.userId;

		// Delete devices in the database that match the query
		const result = await Device.deleteOne({
			deviceId: deviceId,
		});

		await User.findByIdAndUpdate(userId, { $inc: { devices: -1 } });

		// Check if a device was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "Device not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "Device deleted successfully",
		});
	} catch (error) {
		console.error("Error deleting device:", error);
		res.status(500).json({ error: "Failed to delete device" });
	}
};
