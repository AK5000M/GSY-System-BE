import express from "express";
import {
	getUserInfo,
	updateUserInfo,
	deleteUserAccount,
	getAllUsers,
	getAllReSellers,
	updateUserStatus,
	updateUserIP,
	updateUserLicense,
	AddExtraDeviceCount,
	setUserResetPassword,
	addNewReSeller,
	updateReSellerInformation,
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

// Get All ReSellers for Admin
router.get("/admin/user/get-all-resellers", authenticateJwt, getAllReSellers);

// Update User Status by Admin
router.put(
	"/admin/user/status",
	[
		check("userId").notEmpty(),
		check("type").notEmpty(),
		check("manager_Id").notEmpty(),
		check("manager").notEmpty(),
		check("manager_Role").notEmpty(),
	],
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
	[
		check("userId").notEmpty(),
		check("license").notEmpty(),
		check("manager_Id").notEmpty(),
		check("manager").notEmpty(),
		check("manager_Role").notEmpty(),
	],
	authenticateJwt,
	updateUserLicense
);

// Extra Device Count by Admin
router.put(
	"/admin/user/extra-device",
	[
		check("userId").notEmpty(),
		check("extra").notEmpty(),
		check("manager_Id").notEmpty(),
		check("manager").notEmpty(),
		check("manager_Role").notEmpty(),
	],
	authenticateJwt,
	AddExtraDeviceCount
);

// Set Reset Password by Admin
router.put(
	"/admin/user/allow-reset-password",
	[
		check("userId").notEmpty(),
		check("status").notEmpty(),
		check("manager_Id").notEmpty(),
		check("manager").notEmpty(),
		check("manager_Role").notEmpty(),
	],
	authenticateJwt,
	setUserResetPassword
);

// Add New ReSeller
router.post(
	"/admin/user/add-reseller",
	[
		check("username").notEmpty(),
		check("email").notEmpty(),
		check("password").notEmpty(),
		check("manager_Id").notEmpty(),
		check("manager").notEmpty(),
	],
	authenticateJwt,
	addNewReSeller
);

// Set Reset ReSeller
router.put(
	"/admin/user/update-reseller",
	[check("userId").notEmpty(), check("password").notEmpty()],
	authenticateJwt,
	updateReSellerInformation
);

export default router;
