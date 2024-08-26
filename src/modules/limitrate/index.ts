import rateLimit from "express-rate-limit";

//limit email verification and forget password
const minutes = 30;
const emailVerificationLimit = rateLimit({
	windowMs: minutes * 60 * 1000,
	max: 5, ///request will block after 3 request from a ip, you can change this
	standardHeaders: true, // Return rate limit info in the `RateLimit-*` headers
	legacyHeaders: false, // Disable the `X-RateLimit-*` headers
	handler: (req, res) => {
		res.status(429).send({
			status: "error",
			message: `You made too many requests. Please try again after ${minutes} minutes.`,
			error_code: 0x06,
		});
	},
});

const passwordVerificationLimit = rateLimit({
	windowMs: minutes * 60 * 1000,
	max: 5, ///request will block after 3 request from a ip, you can change this
	standardHeaders: true,
	legacyHeaders: false,
	handler: (req, res) => {
		res.status(429).send({
			status: "error",
			message: `You made too many requests. Please try again after ${minutes} minutes.`,
			error_code: 0x07,
		});
	},
});

const limitRateModule = {
	emailVerificationLimit,
	passwordVerificationLimit,
};

export default limitRateModule;
