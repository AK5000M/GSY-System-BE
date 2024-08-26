import { model, Schema } from "mongoose";
import { SocialUsageModelType } from "../utils";

// Create a new Mongoose model for the social usage document
const SocialUsage = model(
	"socialusage",
	// Define the schema for the sms document
	new Schema<SocialUsageModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		userId: {
			type: String,
			required: true,
		},
		applicationName: {
			type: String,
			default: null,
		},
		appOpenedCount: {
			type: Number,
			default: null,
		},
		totalTimeSpent: {
			type: Number,
			default: null,
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
	})
);

export default SocialUsage;
