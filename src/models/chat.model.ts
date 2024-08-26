import { model, Schema } from "mongoose";
import { MessageModelType } from "../utils";

// Create a new Mongoose model for the message document
const Chat = model(
	"messages",
	// Define the schema for the message document
	new Schema<MessageModelType>({
		userId: {
			type: String,
			required: true,
		},
		message: {
			type: String,
			default: null,
		},
		sender: {
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

export default Chat;
