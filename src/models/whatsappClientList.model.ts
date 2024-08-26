import { model, Schema } from "mongoose";
import { SocialListModelType } from "../utils";

// Create a new Mongoose model for the whatsapp clientList usage document
const WhatsappClientList = model(
	"whatsappClientList",
	// Define the schema for the whatsapp client list document
	new Schema<SocialListModelType>({
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
		lastMessage: {
			type: String,
			default: null,
		},
		lastDate: {
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

export default WhatsappClientList;
