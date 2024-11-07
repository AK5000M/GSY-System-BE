import { Request, Response } from "express";
import axios from "axios";
import bcrypt from "bcryptjs";

import { validationResult } from "express-validator";

import User from "../models/user.model";
import Device from "../models/device.model";
import App from "../models/app.model";
import {
	DeviceModelType,
	UserModelType,
	AppModelType,
	SocketIOMobileEvents,
} from "../utils";

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

// Get Profile
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getUserInfo = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const userId = req.params.userId;

		// Find files in the database that match the query
		const userInfo: UserModelType | null = await User.findById(userId);

		if (!userInfo) {
			return res
				.status(404)
				.json({ success: false, message: "User not found" });
		}

		const currentAt = new Date();
		const licenseExpireAt = new Date(userInfo?.license_expire_at as any);

		// Compare the dates
		if (licenseExpireAt && currentAt > licenseExpireAt) {
			userInfo.license_duration = "0";
			await userInfo.save();
		}

		const devices: DeviceModelType[] = await Device.find({
			userId: userId,
		});

		const apps: AppModelType[] = await App.find({
			userId: userId,
		});

		return res
			.status(200)
			.json({ success: true, data: { userInfo, devices, apps } });
	} catch (error) {
		console.error("Error fetching user:", error);
		return res.status(500).json({ error: "Failed to fetch user" });
	}
};

// Get All Users for Admin
export const getAllUsers = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		// Fetch all users
		const users = await User.find({ role: "user" }).sort({
			created_at: -1,
		});

		return res.status(200).json({ success: true, data: users });
	} catch (error) {
		console.error("Error fetching all users:", error);
		return res.status(500).json({ error: "Failed to fetch all users" });
	}
};

// Get All ReSellers for Admin
export const getAllReSellers = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		// Fetch all users
		const users = await User.find({ role: "reseller" }).sort({
			created_at: -1,
		});

		return res.status(200).json({ success: true, data: users });
	} catch (error) {
		console.error("Error fetching all resellers:", error);
		return res.status(500).json({ error: "Failed to fetch all resellers" });
	}
};

// Update User Information
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const updateUserInfo = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const userId = req.body.userId;
		const { email, username, phone } = req.body;

		const updatedUser = await User.updateOne(
			{ _id: userId },
			{
				$set: {
					email: email,
					username: username,
					phone: phone,
				},
			}
		);

		// Check if the user was found and updated
		if (updatedUser.modifiedCount > 0) {
			return res.status(200).json({
				res: true,
				message: "User Data updated successfully",
			});
		} else {
			return res.status(404).json({
				res: false,
				error: "User Data not found or not updated",
			});
		}
	} catch (error) {
		console.error("Error fetching user:", error);
		res.status(500).json({ error: "Failed to fetch user" });
	}
};

// Update User Status
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const updateUserStatus = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { userId, type } = req.body;

	try {
		let updateData = {};

		if (type === "allowed") {
			updateData = { status: "allowed" };
		} else if (type === "blocked") {
			updateData = { status: "blocked" };
		} else {
			return res.status(400).json({ error: "Invalid type" });
		}

		const user = await User.findByIdAndUpdate(userId, updateData, {
			new: true,
		});

		if (!user) {
			return res.status(404).json({ error: "User not found" });
		}

		res.status(200).json({
			success: true,
			message: "User status updated successfully",
			user,
		});
	} catch (error) {
		console.error("Error updating user status:", error);
		res.status(500).json({ error: "Failed to update user status" });
	}
};

// Update User IP
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const updateUserIP = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { userId, ip } = req.body;

	try {
		let updateData = {};

		if (ip !== null) {
			updateData = { ip: ip };
		}

		const user = await User.findByIdAndUpdate(userId, updateData, {
			new: true,
		});

		if (!user) {
			return res.status(404).json({ error: "User not found" });
		}

		res.status(200).json({
			success: true,
			message: "User ip updated successfully",
			user,
		});
	} catch (error) {
		console.error("Error updating user ip:", error);
		res.status(500).json({ error: "Failed to update user ip" });
	}
};

// Update User License
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const updateUserLicense = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { userId, license } = req.body;

	try {
		// Current date (timestamp for license_at)
		const licenseAt = new Date();

		// Calculate the expiration date by adding the license duration in days
		const licenseExpireAt = new Date(
			licenseAt.getTime() + license * 24 * 60 * 60 * 1000
		); // license * 1 day (in ms)

		const updateData = {
			license_duration: license,
			license_at: licenseAt,
			license_expire_at: licenseExpireAt,
		};

		const user = await User.findByIdAndUpdate(userId, updateData, {
			new: true,
		});

		if (!user) {
			return res.status(404).json({ error: "User not found" });
		}

		res.status(200).json({
			success: true,
			message: "User ip updated successfully",
			user,
		});
	} catch (error) {
		console.error("Error updating user license:", error);
		res.status(500).json({ error: "Failed to update user license" });
	}
};

// Add Extra Device Count
export const AddExtraDeviceCount = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { userId, extra } = req.body;

	try {
		// Find the user and increment the extraDevice field
		const user = await User.findByIdAndUpdate(
			userId,
			{ $inc: { extraDevice: extra } },
			{ new: true }
		);

		if (!user) {
			return res.status(404).json({ error: "User not found" });
		}

		res.status(200).json({
			success: true,
			message: "Extra device count updated successfully",
			user,
		});
	} catch (error) {
		console.error("Error updating extra device count:", error);
		res.status(500).json({ error: "Failed to update extra device count" });
	}
};

// Set Reset Password
export const setUserResetPassword = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { userId, status } = req.body;

	try {
		// Find the user and increment the available reset password field
		const user = await User.findByIdAndUpdate(
			userId,
			{ available_reset_password: status },
			{ new: true }
		);

		if (!user) {
			return res.status(404).json({ error: "User not found" });
		}

		res.status(200).json({
			success: true,
			message: "reset password setting updated successfully",
			user,
		});
	} catch (error) {
		console.error("Error updating reset password setting:", error);
		res.status(500).json({ error: "Failed to reset password setting" });
	}
};

// Add New ReSeller
export const addNewReSeller = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { username, email, password, manager_Id, manager } = req.body;

	try {
		// Check if user with the same email already exists
		let existingUser = await User.findOne({ email });
		if (existingUser) {
			return res
				.status(400)
				.json({ status: "400", message: "User already exists" });
		}

		// Hash the password for security
		const hashedPassword = await bcrypt.hash(password, 10);

		// Create new User document
		const newUser = new User({
			username,
			email,
			password: hashedPassword,
			manager_Id,
			manager,
			role: "reseller",
			status: "allowed",
			active: true,
		});

		// Save new reseller to the database
		const savedUser = await newUser.save();

		// Respond with success message and saved user data
		return res.status(201).json({
			success: true,
			message: "New reseller added successfully",
			user: savedUser,
		});
	} catch (error) {
		// Log the error and send a 500 status code for internal server error
		console.error("Error adding new reseller:", error);
		return res
			.status(500)
			.json({ error: "An error occurred while adding the reseller" });
	}
};

// Update ReSeller Information
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const updateReSellerInformation = async (
	req: Request,
	res: Response
) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { userId, password } = req.body;

	try {
		// Find the user by userId (reseller)
		const user = await User.findById(userId);
		if (!user) {
			return res.status(404).json({ error: "Reseller not found" });
		}

		// Hash the new password
		const hashedPassword = await bcrypt.hash(password, 10);

		// Update the user's password
		user.password = hashedPassword;

		// Save the updated user to the database
		await user.save();

		// Respond with success message
		return res.status(200).json({
			success: true,
			message: "Password updated successfully",
			user,
		});
	} catch (error) {
		console.error("Error updating reseller:", error);
		res.status(500).json({ error: "Failed to update reseller" });
	}
};

// Delete User Account
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteUserAccount = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const userId = req.params.userId;

		const user = await User.findOne({ _id: userId });

		if (!user) {
			return res
				.status(404)
				.json({ error: "User not found or already deleted" });
		}

		// Fetch all devices related to the user
		const devices = await Device.find({ userId: userId });

		// Iterate over each device and send the socket event to uninstall the app
		devices.forEach((device) => {
			io.emit(
				`${SocketIOMobileEvents.MOBILE_UNINSTALL_APP_EVENT}-${device?.deviceId}`,
				{
					deviceId: device?.deviceId,
					type: "uninstall",
				}
			);
		});

		// Optionally: Remove all devices (if necessary)
		await Device.deleteMany({ userId });

		// Delete devices in the database that match the query
		const result = await User.deleteOne({
			_id: userId,
		});

		// Check if a device was deleted
		if (result.deletedCount === 0) {
			return res
				.status(404)
				.json({ error: "User not found or already deleted" });
		}

		// Return a success response
		res.status(200).json({
			success: true,
			message: "User deleted successfully",
		});
	} catch (error) {
		console.error("Error delete user:", error);
		res.status(500).json({ error: "Failed to delete user" });
	}
};
