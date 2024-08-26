import { model, Schema } from "mongoose";
import { NotificationsModelType } from "../utils";

// Create a new Mongoose model for the notifications document
const Notification = model(
	"notification",
	// Define the schema for the notifications document
	new Schema<NotificationsModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		title: {
			type: String,
			default: null,
		},
		content: {
			type: String,
			default: null,
		},
		sort: {
			type: String,
			default: null,
		},
		userId: {
			type: String,
			default: null,
		},
		active: {
			type: Boolean,
			default: false,
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
	})
);

export default Notification;
