import { model, Schema } from "mongoose";
import { SocialMessageModelType } from "../utils";

// Create a new Mongoose model for the whatsapp client message usage document
const WhatsappClientMessage = model(
	"whatsappClientMessage",
	// Define the schema for the whatsapp client message document
	new Schema<SocialMessageModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		socialName: {
			type: String,
			default: null,
		},
		phoneNumber: {
			type: String,
			default: null,
		},
		userName: {
			type: String,
			default: null,
		},
		messageText: {
			type: String,
			default: null,
		},
		referenceType: {
			type: String,
			default: null,
		},
		messageDate: {
			type: String,
			default: null,
		},
		messageTime: {
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

export default WhatsappClientMessage;
