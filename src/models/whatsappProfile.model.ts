import { model, Schema } from "mongoose";
import { SocialProfileModelType } from "../utils";

// Create a new Mongoose model for the whatsapp profile usage document
const WhatsappProfile = model(
	"whatsappProfile",
	// Define the schema for the whatsapp profile document
	new Schema<SocialProfileModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		socialName: {
			type: String,
			default: null,
		},
		userName: {
			type: String,
			default: null,
		},
		phoneNumber: {
			type: String,
			default: null,
		},
		stateText: {
			type: String,
			default: null,
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
	})
);

export default WhatsappProfile;
