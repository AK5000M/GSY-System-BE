import { NextFunction, Request, Response } from "express";
import { userDbModule } from "../modules/mongo";

const checkDuplicateEmail = async (
	req: Request,
	res: Response,
	next: NextFunction
) => {
	try {
		const { email } = req.body;
		const user = await userDbModule.getUser({ email: email });
		if (user) {
			return res.status(400).send({
				status: "error",
				message:
					"Registration failed! This email is already in use. Please use a different email or login.",
				error_code: 0x08,
			});
		}
		next();
	} catch (err: unknown) {
		res.status(500).send({
			status: "error",
			message: "500 Server Error",
			error: (err as Error)?.message,
			error_code: 0x00,
		});
	}
};

const checkExsitingEmail = async (
	req: Request,
	res: Response,
	next: NextFunction
) => {
	try {
		const { email } = req.body;
		const user = await userDbModule.getUser({ email: email });
		if (!user) {
			return res.status(400).send({
				status: "error",
				message:
					"User not found. Please register to access the website.",
				error_code: 0x07,
			});
		}
		req.body.user = user;
		next();
	} catch (err: unknown) {
		res.status(500).send({
			status: "error",
			message: "500 Server Error",
			error: (err as Error)?.message,
			error_code: 0x00,
		});
	}
};

const verification = {
	checkDuplicateEmail,
	checkExsitingEmail,
};

export default verification;
