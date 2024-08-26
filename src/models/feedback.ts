import { model, Schema } from "mongoose";
import { FeedbackModelType } from "../utils";

// Create a new Mongoose model for the feedback document
const Feedback = model(
	"feedback",
	// Define the schema for the feedback document
	new Schema<FeedbackModelType>({
		userId: {
			type: String,
			required: true,
		},
		feedback: {
			type: String,
			default: null,
		},
		rate: {
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

export default Feedback;
