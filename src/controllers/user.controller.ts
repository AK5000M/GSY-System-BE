import { Request, Response } from "express";
import axios from "axios";

import { validationResult } from "express-validator";

import { createCanvas } from "canvas";
import * as QRCode from "qrcode";
import User from "../models/user.model";
import Device from "../models/device.model";
import App from "../models/app.model";
import { DeviceModelType, UserModelType, AppModelType } from "../utils";
import {
	createCardToken,
	newSubscription,
	getSubscription,
	// createNewSubscribe,
	// createNewPIXPayment,
	// getNewPIXCode,
	// createNewPixPaymentURL,
} from "../modules/asaas";

const generateRoomId = () => {
	// Generate a ws room ID
	var randomstring = require("randomstring");
	const createRoomId = randomstring.generate({
		length: 12,
		charset: "alphabetic",
	});
	return createRoomId;
};

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

// Add Extra Device Count
export const AddExtraDeviceCount = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { userId, extraCount } = req.body;

	try {
		// Find the user and increment the extraDevice field
		const user = await User.findByIdAndUpdate(
			userId,
			{ $inc: { extraDevice: extraCount } },
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

// Check Customer of Payment API
// export const customerChecking = async (req: Request, res: Response) => {
// 	try {
// 		const customer = req.body;
// 		const userId = customer?.externalReference;

// 		// Ensure externalReference (userId) is provided
// 		if (!userId) {
// 			return res.status(400).json({
// 				success: false,
// 				message: "externalReference (userId) is required",
// 			});
// 		}

// 		// Check if the customer exists or create a new one
// 		const result = await customerData(customer);

// 		// Prepare the data to update the user with the customerId
// 		// const updateData = { customerId: result?.id };

// 		// Update the user document with the customerId
// 		// const user = await User.findByIdAndUpdate(
// 		// 	{ _id: result?.externalReference },
// 		// 	updateData,
// 		// 	{
// 		// 		new: true,
// 		// 	}
// 		// );
// 		// console.log("customerInfo:", user);
// 		// // Check if the user was found and updated
// 		// if (!user) {
// 		// 	return res.status(404).json({
// 		// 		success: false,
// 		// 		message: "User not found",
// 		// 	});
// 		// }

// 		// Respond with success
// 		// res.status(200).json({
// 		// 	success: true,
// 		// 	message: "CustomerId added successfully",
// 		// 	user,
// 		// });
// 	} catch (error) {
// 		console.error("Error customer checking:", error);
// 		res.status(500).json({ error: "Failed to check or create a customer" });
// 	}
// };

// Create Credit Token
export const createCreditCardToken = async (req: Request, res: Response) => {
	try {
		const data = req.body;

		const creditCardToken = await createCardToken(data);

		if (creditCardToken) {
			res.status(200).json({
				success: true,
				message: "Credit Card Token created successfully",
				creditCardToken,
			});
		} else {
			res.status(403).json({
				success: false,
				message: "Credit Card Token failed",
			});
		}
	} catch (error) {
		console.log(error);
	}
};

// Create new Subscription
export const createNewSubscription = async (req: Request, res: Response) => {
	try {
		const data = req.body;
		const createdNewSubscription = await newSubscription(data);

		if (createdNewSubscription.status == 200) {
			const subscribeConfirmData = createdNewSubscription.data;
			res.status(200).json({
				success: true,
				message: "New Subscription created successfully",
				subscribeConfirmData,
			});
		}
	} catch (error) {
		res.status(500).json({
			success: false,
			message: "New Subscription failed",
		});
	}
};

// Get New Subscription
export const getNewSubscription = async (req: Request, res: Response) => {
	try {
		const subscriptionId = req.params.subscriptionId;

		const newSubscription = await getSubscription(subscriptionId);

		if (newSubscription.status == 200) {
			const newSubsciptionData = newSubscription.data;

			res.status(200).json({
				success: true,
				message: "Get new subscription successfully",
				newSubsciptionData,
			});
		}
	} catch (error) {
		res.status(500).json({
			success: false,
			message: "get new subscription failed",
		});
	}
};

// Receive Subscribe by WebHooks
export const paymentCallBack = async (req: Request, res: Response) => {
	try {
		const body = req.body;
		console.log("webhooks:", body);
		if (
			body.event === "PAYMENT_RECEIVED" ||
			body.event === "PAYMENT_CONFIRMED"
		) {
			const payment = body.payment;
			const customerId = payment.customer; // Assuming this is a string
			const description = payment.description;

			let updateData = {};

			if (description === "Plano Básico da TechDroid") {
				updateData = { subscribe: "basic", status: "allowed" };
			} else if (description === "Plano Premium da TechDroid") {
				updateData = { subscribe: "basic", status: "allowed" };
			} else {
				res.status(400).json({
					success: false,
					message: "Unrecognized payment description",
				});
				return;
			}
			// Call the function to update user subscription
			const updatedUser = await User.findOneAndUpdate(
				{ customerId: customerId }, // Find user by customerId field
				updateData,
				{
					new: true,
				}
			);

			// console.log("updatedUser=>:", updatedUser);

			if (updatedUser) {
				res.status(200).json({
					success: true,
					message:
						body.event === "PAYMENT_RECEIVED"
							? "Payment received successfully"
							: "Subscription confirmed successfully",
					updatedUser,
				});
			} else {
				res.status(404).json({
					success: false,
					message: "User not found",
				});
			}

			return;
		} else {
			console.log(`This event is not accepted: ${body.event}`);
		}

		// Return a response to indicate the webhook was received
		res.json({ received: true });
	} catch (error) {
		console.error(error);
		res.status(500).json({
			success: false,
			message: "Internal Server Error",
		});
	}
};
// User Subscribe Option Update by Webhooks
export const updateUserSubscription = async (
	customerId: string,
	description: string
) => {
	try {
		let updateData = {};

		if (description === "Plano Básico da TechDroid") {
			updateData = { subscribe: "basic" };
		} else if (description === "Plano Premium da TechDroid") {
			updateData = { subscribe: "premium" };
		} else {
			console.log(`Unrecognized payment description: ${description}`);
			return null;
		}

		// Update user subscription in the database
		const updatedUser = await User.findByIdAndUpdate(
			customerId,
			updateData,
			{
				new: true,
			}
		);

		if (updatedUser) {
			console.log(
				`User subscription updated to ${updatedUser.subscribe} for user ID: ${customerId}`
			);
		} else {
			console.log(
				`Failed to update user subscription for user ID: ${customerId}`
			);
		}

		return updatedUser;
	} catch (error) {
		console.error("Error updating user subscription:", error);
		return null;
	}
};

export const cancelPlan = async (req: Request, res: Response) => {
	try {
		const data = req.body;
		const userId = data._id;

		if (!userId) {
			return res.status(400).json({
				success: false,
				message: "Customer ID is required",
			});
		}

		// Update user subscription in the database
		const updatedUser = await User.findByIdAndUpdate(
			userId,
			{ subscribe: "none" },
			{
				new: true, // Return the updated document
				runValidators: true, // Ensure that validators are run
			}
		);

		if (!updatedUser) {
			return res.status(404).json({
				success: false,
				message: "User not found",
			});
		}

		res.status(200).json({
			success: true,
			message: "Subscription canceled successfully",
			updatedUser,
		});
	} catch (error) {
		console.error("Error canceling subscription:", error);
		res.status(500).json({
			success: false,
			message: "Cancel Plan failed",
		});
	}
};

//  ************* I don't use these functions currently *********************
// // User Subscribe
// export const createSubscription = async (req: Request, res: Response) => {
// 	try {
// 		const data = req.body;

// 		const createSubscribe = await createNewSubscribe(data);

// 		// if (createSubscribe.status == 200) {
// 		// 	const subscribeConfirmData = createSubscribe.data;

// 		// 	res.status(200).json({
// 		// 		success: true,
// 		// 		message: "Credit Card Subscription created successfully",
// 		// 		subscribeConfirmData,
// 		// 	});
// 		// }
// 	} catch (error) {
// 		res.status(500).json({
// 			success: false,
// 			message: "Credit Card Subscription failed",
// 		});
// 	}
// };

// // Create PIX Payment
// export const createPIXPayment = async (req: Request, res: Response) => {
// 	try {
// 		const data = req.body;
// 		const newPIXPayment = await createNewPIXPayment(data);

// 		if (newPIXPayment.status == 200) {
// 			const createdPIXPayement = newPIXPayment.data;

// 			res.status(200).json({
// 				success: true,
// 				message: "PIX payment created successfully",
// 				createdPIXPayement,
// 			});
// 		}
// 	} catch (error) {
// 		res.status(500).json({
// 			success: false,
// 			message: "PIX Payment failed",
// 		});
// 	}
// };

// // Get PIX Code
// export const getPIXCode = async (req: Request, res: Response) => {
// 	try {
// 		const paymentId = req.params.paymentId;

// 		const newPIXCode = await getNewPIXCode(paymentId);

// 		if (newPIXCode.status == 200) {
// 			const PIXCode = newPIXCode.data;

// 			res.status(200).json({
// 				success: true,
// 				message: "PIX Code created successfully",
// 				PIXCode,
// 			});
// 		}
// 	} catch (error) {
// 		res.status(500).json({
// 			success: false,
// 			message: "get PIX Code failed",
// 		});
// 	}
// };

// // Get PIX payment URL
// export const getPixPaymentUrl = async (req: Request, res: Response) => {
// 	try {
// 		const data = req.body;
// 		const pixPaymentData = await createNewPixPaymentURL(data);
// 		console.log({ pixPaymentData });
// 		if (pixPaymentData.status == 200) {
// 			const newPIXPaymentdata = pixPaymentData.data;

// 			res.status(200).json({
// 				success: true,
// 				message: "PIX payment URL created successfully",
// 				newPIXPaymentdata,
// 			});
// 		}
// 	} catch (error) {
// 		res.status(500).json({
// 			success: false,
// 			message: "PIX Payment URL failed",
// 		});
// 	}
// };

//  ************* End *********************

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
		const userId = req.body.userId;
		// Delete the user from the database
		const deleteResult = await User.updateOne(
			{
				_id: userId,
			},
			{
				$set: {
					active: false,
				},
			}
		);

		// Check if the notification was found and deleted
		if (deleteResult.modifiedCount > 0) {
			return res
				.status(200)
				.json({ message: "User account deleted successfully" });
		} else {
			return res
				.status(404)
				.json({ error: "User account not found or not deleted" });
		}
	} catch (error) {
		console.error("Error delete user:", error);
		res.status(500).json({ error: "Failed to delete user" });
	}
};

// Generate QR code
export const generateQRCode = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const userId = req.params.userId;

		// Find files in the database that match the query
		const userInfo: UserModelType | null = await User.findById(userId);

		const username = userInfo?.username;
		const email = userInfo?.email;

		// Combine user info into a single string
		const userInfoForQRCode = {
			userId: userId,
			username: username,
			email: email,
		};

		// Generate QR code
		const qrCanvas = createCanvas(500, 500);
		await QRCode.toCanvas(qrCanvas, JSON.stringify(userInfoForQRCode));

		// Convert canvas to base64 data URL
		const qrDataURL = qrCanvas.toDataURL();

		res.send({ qrCode: qrDataURL });
	} catch (error) {
		console.error("Error fetching user:", error);
		res.status(500).json({ error: "Failed to fetch user" });
	}
};
