import { Request, Response } from "express";
import { validationResult } from "express-validator";
import randomstring from "randomstring";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";

import User from "../models/user.model";
import { UserModelType } from "../utils";
import template from "../config/verify-template";

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

		const hashedPassword = await bcrypt.hash(password, 10);
		user = new User({
			email,
			password: hashedPassword,
			username,
			ip,
		});

		const savedUser = await user.save();

		// Create Customer in Payment
		if (savedUser) {
			// Create token
			const payload = { id: savedUser.id };
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

	const { email, password, role, ip } = req.body;
	try {
		const user = await User.findOne({ email, role });
		if (!user) {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}

		if (user.ip == null) {
			user.ip = ip;
			await user.save();
		} else if (user.ip !== ip) {
			return res
				.status(401)
				.json({ status: "401", message: "IP incorrect" });
		}

		const isMatch = await bcrypt.compare(password, user.password as string);
		if (!isMatch) {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}

		const payload = { id: user.id };

		const token = jwt.sign(payload, process.env.JWT_SECRET as string, {
			expiresIn: "10h",
		});

		res.status(201).json({
			status: "201",
			data: { user: user, token: token },
		});
	} catch (error: any) {
		console.error(error.message);
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

	const { email, password, role } = req.body;
	try {
		const user = await User.findOne({ email, role });
		if (!user) {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}

		const isMatch = await bcrypt.compare(password, user.password as string);
		if (!isMatch) {
			return res
				.status(400)
				.json({ status: "400", message: "Invalid credentials" });
		}

		const payload = { id: user.id };

		const token = jwt.sign(payload, process.env.JWT_SECRET as string, {
			expiresIn: "10h",
		});

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
		const { email } = req.body;
		const user: UserModelType | null = await User.findOne({ email });

		if (!user) {
			return res
				.status(404)
				.json({ status: "404", message: "User not found" });
		}

		const newPassword = randomstring.generate({
			length: 10,
			charset: "alphanumeric",
		});
		console.log(
			`😂`,
			"\x1b[35m GhostSpy New Password \x1b[0m",
			newPassword
		);
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

		const html = template.resetPasswordTemplate(newPassword);
		const subject = "Password Reset from TechDroid";
		const message = "Please check your email to reset password.";

		// await mailServer.sendEmail(
		// 	res, // Pass the 'res' object directly
		// 	FROM_EMAIL as string,
		// 	email,
		// 	subject,
		// 	html,
		// 	message,
		// 	token
		// );

		res.status(200).json({
			status: "200",
			message: "New password set email sent",
		});
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
