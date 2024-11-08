import { Request, Response } from "express";
import { validationResult } from "express-validator";
import randomstring from "randomstring";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";

import User from "../models/user.model";
import Session from "../models/session.model";

import { UserModelType } from "../utils";
import template from "../config/verify-template";

import { v4 as uuidv4 } from "uuid"; // Import UUID generator
import { use } from "passport";

// Auth Register
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const register = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	const { email, password, username, ip } = req.body;

	try {
		let user = await User.findOne({ email });
		if (user) {
			return res
				.status(400)
				.json({ status: "400", message: "User already exists" });
		}

		let admin = await User.findOne({ role: "admin" });

		const hashedPassword = await bcrypt.hash(password, 10);
		user = new User({
			email,
			password: hashedPassword,
			username,
			ip,
			manager_Id: admin?._id,
			manager: admin?.username,
			manager_Role: admin?.role,
		});

		const savedUser = await user.save();

		// Create Customer in Payment
		if (savedUser) {
			const sessionId = uuidv4();
			const payload = { id: savedUser.id, sessionId };

			user.session_Id = sessionId;
			await user.save();

			const token = jwt.sign(payload, process.env.JWT_SECRET as string, {
				expiresIn: "10h",
			});

			res.status(201).json({
				status: "201",
				data: { user: savedUser, token: token },
				message: "User registered successfully",
			});
		}
	} catch (error: any) {
		console.error("Error during user registration:", error.message);
		res.status(500).json({ message: "Server error" });
	}
};

// Auth Login
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const login = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { email, password } = req.body;

	try {
		const user = await User.findOne({ email });
		const currentAt = new Date();
		const licenseExpireAt = new Date(user?.license_expire_at as any);

		if (!user) {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}

		if (user.role !== "user") {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}

		// Compare the dates
		if (licenseExpireAt && currentAt > licenseExpireAt) {
			return res
				.status(403)
				.json({ status: "403", message: "License expired" });
		}

		// if (user.ip == null) {
		// 	user.ip = ip;
		// 	await user.save();
		// } else if (user.ip !== ip) {
		// 	return res
		// 		.status(401)
		// 		.json({ status: "401", message: "IP incorrect" });
		// }

		const isMatch = await bcrypt.compare(password, user.password as string);
		if (!isMatch) {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}
		// Session ID for Login
		const sessionId = uuidv4();
		const payload = { id: user.id, sessionId };

		user.session_Id = sessionId;
		await user.save();

		const token = jwt.sign(payload, process.env.JWT_SECRET as string, {
			expiresIn: "10h",
		});

		res.status(201).json({
			status: "201",
			data: { user: user, token: token },
		});
	} catch (error: any) {
		res.status(500).json({ message: "Server error" });
	}
};

// Admin Auth Login
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const adminLogin = async (req: Request, res: Response) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { email, password } = req.body;
	try {
		const user: UserModelType | any = await User.findOne({ email });
		if (!user) {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}

		if (user.role !== "admin" && user.role !== "reseller") {
			return res
				.status(403)
				.json({ status: "403", message: "Access denied" });
		}

		const isMatch = await bcrypt.compare(password, user.password as string);
		if (!isMatch) {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}

		const sessionId = uuidv4();
		const payload = { id: user.id, sessionId };

		const token = jwt.sign(payload, process.env.JWT_SECRET as string, {
			expiresIn: "10h",
		});

		// Find users with expired licenses and update their license_duration to 0
		const currentDate = new Date();
		await User.updateMany(
			{
				license_expire_at: { $lt: currentDate },
				license_duration: { $ne: 0 },
			},
			{ $set: { license_duration: 0 } }
		);

		res.status(201).json({
			status: "201",
			data: { user: user, token: token },
		});
	} catch (error: any) {
		console.error(error.message);
		res.status(500).json({ message: "Server error" });
	}
};

// Auth Forgot Password
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const forgotPassword = async (req: Request, res: Response) => {
	try {
		const { email, password } = req.body;
		const user: UserModelType | null = await User.findOne({ email });

		if (!user) {
			return res
				.status(404)
				.json({ status: "404", message: "User not found" });
		}
		if (user.available_reset_password == false) {
			return res.status(403).json({
				status: "403",
				message: "Reset Password isn't available",
			});
		} else {
			const hashedPassword = await bcrypt.hash(password, 10);

			// Update user's password in the database
			user.password = hashedPassword;
			user.available_reset_password = false;
			await user.save();

			res.status(200).json({
				status: "200",
				message: "New password set email sent",
			});
		}
	} catch (error) {
		res.status(500).json({
			status: "500",
			message: "Internal server error",
		});
	}
};

// Auth Reset Password
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const resetPassword = async (req: Request, res: Response) => {
	try {
		// Check for validation errors
		const errors = validationResult(req);
		if (!errors.isEmpty()) {
			return res.status(400).json({ errors: errors.array() });
		}
		const { email, oldPassword, newPassword } = req.body;

		// Find the user by email
		const user: UserModelType | null = await User.findOne({ email });
		if (!user) {
			return res.status(404).json({ message: "User not found" });
		}

		// Check if the old password matches the user's current password
		const isMatch = await bcrypt.compare(oldPassword, user.password as any);
		if (!isMatch) {
			return res
				.status(400)
				.json({ message: "Current password is incorrect" });
		}

		// Encrypt the new password
		const hashedPassword = await bcrypt.hash(newPassword, 10);

		// Update user's password in the database
		user.password = hashedPassword;
		await user.save();

		const token = jwt.sign(
			{ email: email },
			process.env.JWT_SECRET as string,
			{
				expiresIn: "30m",
			}
		);

		const html = template.updatedPasswordTemplate();
		const subject = "Password Update";
		const message = "Please check your email to update password.";

		// mailServer.sendEmail(
		// 	res,
		// 	FROM_EMAIL as string,
		// 	email,
		// 	subject,
		// 	html,
		// 	message,
		// 	token
		// );

		res.status(200).json({ message: "Password reset successfully" });
	} catch (error) {
		console.error("Reset password error:", error);
		res.status(500).json({ message: "Internal server error" });
	}
};

// Auth SignOut
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const signout = async (req: Request, res: Response) => {
	try {
		res.cookie("token", "", { expires: new Date(0) });
		// Respond with a success message
		res.status(200).json({ message: "User signed out successfully" });
	} catch (error) {
		console.error("Signout error:", error);
		res.status(500).json({ message: "Internal server error" });
	}
};
