import express from "express";
import {
	register,
	login,
	forgotPassword,
	resetPassword,
	signout,
} from "../controllers/auth.controller";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Register route
router.post(
	"/auth/register",
	[
		check("username").notEmpty(),
		check("email").isEmail(),
		check("password").isLength({ min: 8 }),
	],
	register
);

// Login route
router.post(
	"/auth/login",
	[check("email").isEmail(), check("password").notEmpty()],
	login
);

// ForgotPassword route
router.post(
	"/auth/forgot-password",
	[check("email").isEmail()],
	forgotPassword
);

// ResetPassword route
router.post(
	"/auth/reset-password",
	[
		check("email").isEmail(),
		check("oldPassword").notEmpty(),
		check("newPassword").isLength({ min: 6 }),
	],
	authenticateJwt,
	resetPassword
);

// SignOut route
router.post("/auth/signout", authenticateJwt, signout);

export default router;
