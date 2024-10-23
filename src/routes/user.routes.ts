import express from "express";
import {
	getUserInfo,
	updateUserInfo,
	deleteUserAccount,
	getAllUsers,
	updateUserStatus,
	updateUserIP,
	updateUserLicense,
	AddExtraDeviceCount,
	setUserResetPassword,
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

// Update User Info from Profile
router.put(
	"/user/update",
	[check("userId").notEmpty()],
	authenticateJwt,
	updateUserInfo
);

// Delete User Account
router.delete(
	"/user/delete/:userId",
	[check("userId").notEmpty()],
	authenticateJwt,
	deleteUserAccount
);

// *********************** Admin API

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

// Update User License by Admin
router.put(
	"/admin/user/license",
	[check("userId").notEmpty()],
	authenticateJwt,
	updateUserLicense
);

// Extra Device Count by Admin
router.put(
	"/admin/user/extra-device",
	[check("userId").notEmpty(), check("extra").notEmpty()],
	authenticateJwt,
	AddExtraDeviceCount
);

// Set Reset Password by Admin
router.put(
	"/admin/user/allow-reset-password",
	[check("userId").notEmpty(), check("status").notEmpty()],
	authenticateJwt,
	setUserResetPassword
);

export default router;
