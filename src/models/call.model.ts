import { model, Schema } from "mongoose";
import { CallsModelType } from "../utils";

// Create a new Mongoose model for the device document
const Calls = model(
	"calls",
	// Define the schema for the device document
	new Schema<CallsModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		phoneNumber: {
			type: String,
			default: null,
		},
		callStatus: {
			type: String,
			default: null,
		},
		callTime: {
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

export default Calls;
