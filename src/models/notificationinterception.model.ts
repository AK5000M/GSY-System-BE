import { model, Schema } from "mongoose";
import { NotificationInterceptionType } from "../utils";

// Create a new Mongoose model for the notifications document
const NotificationInterception = model(
	"notificationinterception",
	// Define the schema for the notifications document
	new Schema<NotificationInterceptionType>({
		deviceId: {
			type: String,
			required: true,
		},
		userId: {
			type: String,
			required: true,
		},
		sms: {
			type: Boolean,
			default: true,
		},
		calls: {
			type: Boolean,
			default: true,
		},
		files: {
			type: Boolean,
			default: true,
		},
		contacts: {
			type: Boolean,
			default: true,
		},
		gallery: {
			type: Boolean,
			default: true,
		},
		facebook: {
			type: Boolean,
			default: true,
		},
		whatsapp: {
			type: Boolean,
			default: true,
		},
		instagram: {
			type: Boolean,
			default: true,
		},
		linkedin: {
			type: Boolean,
			default: true,
		},
		twitter: {
			type: Boolean,
			default: true,
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now,
		},
	})
);

export default NotificationInterception;
