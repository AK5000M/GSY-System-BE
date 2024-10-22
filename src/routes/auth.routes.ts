import express from "express";
import {
	login,
	register,
	forgotPassword,
	resetPassword,
	signout,
	adminLogin,
} from "../controllers/auth.controller";
import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";
import { verificationLimit } from "../modules/limitrate";

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

// Login route (apply the email-based rate limiter here)
router.post(
	"/auth/login",
	verificationLimit,
	[check("email").isEmail(), check("password").notEmpty()],
	login
);

// ForgotPassword route (apply the same rate limiter)
router.post(
	"/auth/forgot-password",
	verificationLimit,
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

// Admin Login route
router.post(
	"/auth/admin/login",
	[(check("email").isEmail(), check("password").notEmpty())],
	adminLogin
);

export default router;
