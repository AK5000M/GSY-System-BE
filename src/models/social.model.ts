import { model, Schema } from "mongoose";
import { SocialModelType } from "../utils";

// Create a new Mongoose model for the social usage document
const Social = model(
	"social",
	// Define the schema for the sms document
	new Schema<SocialModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		userId: {
			type: String,
			required: true,
		},
		socialName: {
			type: String,
			default: null,
		},
		socialLink: {
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

export default Social;
