import { model, Schema } from "mongoose";
import { SMSModelType } from "../utils";

// Create a new Mongoose model for the sms document
const SMS = model(
	"sms",
	// Define the schema for the sms document
	new Schema<SMSModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		messages: {
			type: String,
			default: null,
		},
		sender: {
			type: String,
			default: null,
		},
		receiver: {
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

export default SMS;
