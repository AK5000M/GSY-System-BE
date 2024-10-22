// modules/limitrate.ts
import rateLimit from "express-rate-limit";

const minutes = 10;

const verificationLimit = rateLimit({
	windowMs: minutes * 60 * 1000, // 10 minutes
	max: 10, // Limit each email to 5 login attempts per windowMs
	standardHeaders: true, // Return rate limit info in the `RateLimit-*` headers
	legacyHeaders: false, // Disable the `X-RateLimit-*` headers
	keyGenerator: (req) => req.body.email, // Limit based on the email address
	handler: (req, res) => {
		res.status(429).send({
			status: "error",
			message: `Too many login attempts for this email. Please try again after ${minutes} minutes.`,
			error_code: 0x06,
		});
	},
	skip: (req) => !req.body.email, // Skip rate limiting if email is not provided
});

const deviceRateLimiter = rateLimit({
	windowMs: minutes * 60 * 1000, // 10 minutes
	max: 100, // Limit each deviceId to 5 requests per windowMs
	standardHeaders: true,
	legacyHeaders: false,
	keyGenerator: (req) => req.body.deviceId || req.params.deviceId,
	handler: (req, res) => {
		res.status(429).send({
			status: "error",
			message: `Too many requests for this device. Please try again after ${minutes} minutes.`,
			error_code: 0x06,
		});
	},
	skip: (req) => !req.body.deviceId && !req.params.deviceId,
});

const userRateLimiter = rateLimit({
	windowMs: minutes * 60 * 1000, // 10 minutes
	max: 100, // Limit each userId to 5 requests per windowMs
	standardHeaders: true,
	legacyHeaders: false,
	keyGenerator: (req) => req.params.userId,
	handler: (req, res) => {
		res.status(429).send({
			status: "error",
			message: `Too many requests for this user. Please try again after ${minutes} ${req.params.userId} minutes.`,
			error_code: 0x06,
		});
	},
	skip: (req) => !req.params.userId,
});

export { verificationLimit, deviceRateLimiter, userRateLimiter };
