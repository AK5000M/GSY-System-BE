import { model, Schema } from "mongoose";
import { ContactModelType } from "../utils";

// Create a new Mongoose model for the contact document
const Contact = model(
	"contact",
	// Define the schema for the contact document
	new Schema<ContactModelType>({
		fullName: {
			type: String,
			required: true,
		},
		email: {
			type: String,
			default: null,
		},
		subject: {
			type: String,
			default: null,
		},
		message: {
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

export default Contact;
