import express from "express";
import {
	addNewSocial,
	getSocialList,
	filterSocialList,
	deleteOneSocial,
	deleteAllSocials,
	getWhatsappClientList,
	getWhatsappClientMessage,
	getInstagramClientList,
	getInstagramClientMessage,
} from "../controllers/social.controller";

import { check } from "express-validator";
import { authenticateJwt } from "../middleware/auth.middleware";

const router = express.Router();

// Create New Social Usage
router.post(
	"/social/add",
	[
		check("deviceId").notEmpty(),
		check("userId").notEmpty(),
		check("socialName").notEmpty(),
		check("socialLink").notEmpty(),
	],
	authenticateJwt,
	addNewSocial
);

// Get Socials List
router.get(
	"/social/get",
	[check("deviceId").notEmpty(), check("userId").notEmpty()],
	authenticateJwt,
	getSocialList
);

// ******************* Whatsapp API ************************
// Get Whatsapp Client List
router.get(
	"/whatsapp-client-list/get/:deviceId/:social",
	[check("deviceId").notEmpty(), check("social").notEmpty()],
	authenticateJwt,
	getWhatsappClientList
);

// Get Whatsapp Client Message
router.get(
	"/whatsapp-client-message/get/:deviceId/:social/:identifier",
	[
		check("deviceId").notEmpty(),
		check("social").notEmpty(),
		check("identifier").notEmpty(),
	],
	authenticateJwt,
	getWhatsappClientMessage
);

// ****************** Instagram API *************************
router.get(
	"/instagram-client-list/get/:deviceId/:social",
	[check("deviceId").notEmpty(), check("social").notEmpty()],
	authenticateJwt,
	getInstagramClientList
);

// Get Instagram Client Message
router.get(
	"/instagram-client-message/get/:deviceId/:social/:username",
	[
		check("deviceId").notEmpty(),
		check("social").notEmpty(),
		check("username").notEmpty(),
	],
	authenticateJwt,
	getInstagramClientMessage
);

//Filter Social List by Name
router.get(
	"/social/filter",
	[check("deviceId").notEmpty(), check("socialName").notEmpty()],
	authenticateJwt,
	filterSocialList
);

// Delete One Social List
router.delete(
	"/social/delete",
	[check("socialId").notEmpty()],
	authenticateJwt,
	deleteOneSocial
);

// Delete All Social Lists
router.delete(
	"/social/delete-all",
	[check("deviceId").notEmpty()],
	authenticateJwt,
	deleteAllSocials
);

export default router;
