import express from "express";
import { addNewContact, getContacts } from "../controllers/contact.controller";

import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// send files
router.post(
	"/contact/send",
	[
		check("fullName").notEmpty(),
		check("email").notEmpty(),
		check("subject").notEmpty(),
		check("message").notEmpty(),
	],
	addNewContact
);

// get files
router.get(
	"/contact/get",
	[check("deviceId").notEmpty(), check("sender").notEmpty()],
	authenticateJwt,
	getContacts
);

export default router;
