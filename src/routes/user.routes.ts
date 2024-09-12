import express from "express";
import {
	getUserInfo,
	updateUserInfo,
	deleteUserAccount,
	generateQRCode,
	getAllUsers,
	updateUserStatus,
	updateUserIP,
	AddExtraDeviceCount,
	createNewSubscription,
	getNewSubscription,
	paymentCallBack,
	cancelPlan,
	// customerChecking,
	// createSubscription,
	// createCreditCardToken,
	// createPIXPayment,
	// getPIXCode,
	// getPixPaymentUrl,
} from "../controllers/user.controller";

import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Get User Profile
router.get(
	"/user/get/:userId",
	[check("userId").notEmpty()],
	authenticateJwt,
	getUserInfo
);

// Generate QR code
router.get(
	"/user/generate-qr/:userId",
	[check("userId").notEmpty()],
	authenticateJwt,
	generateQRCode
);

// Update User Info from Profile
router.put(
	"/user/update",
	[check("userId").notEmpty()],
	authenticateJwt,
	updateUserInfo
);

// User Customer Checking
// router.post(
// 	"/user/customer",
// 	[check("name").notEmpty(), check("email").notEmpty()],
// 	authenticateJwt,
// 	customerChecking
// );

// Create New Subscription
router.post(
	"/user/create-new-subscribe",
	[check("customer").notEmpty()],
	authenticateJwt,
	createNewSubscription
);

// Get New Subscription
router.get(
	"/user/get-new-subscription/:subscriptionId",
	[check("paymentId").notEmpty()],
	authenticateJwt,
	getNewSubscription
);

// Update User Plan from WebHooks
router.post("/user/payment-webhook", paymentCallBack);

// Cancel a Plan
router.post(
	"/user/cancel-plan",
	[check("userId").notEmpty()],
	authenticateJwt,
	cancelPlan
);

// *********** I don't use these functions currently **************
// // User Subscribe
// router.post(
// 	"/user/create-card-token",
// 	[check("customer").notEmpty()],
// 	authenticateJwt,
// 	createCreditCardToken
// );

// // User Subscribe
// router.post(
// 	"/user/create-subscribe",
// 	[check("customer").notEmpty()],
// 	authenticateJwt,
// 	createSubscription
// );

// // Create PIX payment
// router.post("/user/create-pix-payment", authenticateJwt, createPIXPayment);

// // Get PIX code
// router.get(
// 	"/user/get-pix-payment/:paymentId",
// 	[check("paymentId").notEmpty()],
// 	authenticateJwt,
// 	getPIXCode
// );

// // Get PIX payment URL
// router.post("/user/payment-pixUrl", authenticateJwt, getPixPaymentUrl);
// ********************** End **********************

// Delete User Account
router.delete(
	"/user/delete",
	[check("userId").notEmpty()],
	authenticateJwt,
	deleteUserAccount
);

// Get All Users for Admin
router.get("/admin/user/get-all-users", authenticateJwt, getAllUsers);

// Update User Status by Admin
router.put(
	"/admin/user/status",
	[check("userId").notEmpty()],
	authenticateJwt,
	updateUserStatus
);

// Update User IP by Admin
router.put(
	"/admin/user/ip",
	[check("userId").notEmpty()],
	authenticateJwt,
	updateUserIP
);

// Extra Device Count by Admin
router.put(
	"/admin/user/extra-device",
	[check("userId").notEmpty(), check("extraCount").notEmpty()],
	authenticateJwt,
	AddExtraDeviceCount
);

export default router;
