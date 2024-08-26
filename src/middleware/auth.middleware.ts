import passport from "../config/passport-config";
import { Request, Response, NextFunction } from "express";

import { UserModelType } from "../utils";

export const authenticateJwt = (
	req: Request,
	res: Response,
	next: NextFunction
) => {
	passport.authenticate(
		"jwt",
		{ session: false },
		(err: any, user: UserModelType, info: any) => {
			if (err) {
				return next(err);
			}
			if (!user) {
				return res.status(401).json({ message: "Unauthorized" });
			}
			req.user = user;
			next();
		}
	)(req, res, next);
};
