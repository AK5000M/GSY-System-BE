import { Application, Request, Response } from "express";
import fs from "fs";
import path from "path";

import authRoutes from "./auth.routes";
import userRoutes from "./user.routes";
import chatRoutes from "./chat.routes";
import deviceRoutes from "./device.routes";
import smsRoutes from "./sms.routes";
import callsRoutes from "./calls.routes";
import fileRoutes from "./file.routes";
import contactRoutes from "./contact.routes";
import appRoutes from "./app.routes";
import galleryRoutes from "./gallery.routes";
import addNotificationRoutes from "./notifications.routes";
import socialUsageRoutes from "./socialusage.routes";
import socialRoutes from "./social.routes";
import notificationInterceptionRoutes from "./notificationinterception.routes";

const { API_VER } = process.env;

const initializRoutes = (app: Application) => {
	//
	// Configure default route handlers for GET and POST requests
	app.post(`/api`, (req: Request, res: Response) => {
		res.json({ message: `Welcome to TechDroid ${API_VER} application.` });
	});

	app.get(`/avatars/:file`, (req: Request, res: Response) => {
		const { file } = req.params;
		if (file) {
			const fullpath = path.resolve(
				__dirname,
				`../../public/storage/avatars/${file}`
			);
			if (fs.existsSync(fullpath)) {
				return res.sendFile(fullpath);
			}
		}
		res.send(null);
	});

	app.get(`/companions/:file`, (req: Request, res: Response) => {
		const { file } = req.params;
		if (file) {
			const fullpath = path.resolve(
				__dirname,
				`../../public/storage/companions/${file}`
			);
			if (fs.existsSync(fullpath)) {
				return res.sendFile(fullpath);
			}
		}
		res.send(null);
	});

	app.use(`/api/${API_VER}`, [
		authRoutes,
		userRoutes,
		chatRoutes,
		deviceRoutes,
		smsRoutes,
		callsRoutes,
		fileRoutes,
		contactRoutes,
		appRoutes,
		galleryRoutes,
		addNotificationRoutes,
		socialUsageRoutes,
		socialRoutes,
		notificationInterceptionRoutes,
	]);
};

const routes = {
	initializRoutes,
};
export default routes;
