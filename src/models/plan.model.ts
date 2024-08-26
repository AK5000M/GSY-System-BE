import { model, Schema } from "mongoose";
import { PlanModelType } from "../utils";

// Create a new Mongoose model for the device document
const Plan = model(
	"plan",
	// Define the schema for the device document
	new Schema<PlanModelType>({
		reason: {
			type: String,
			default: null,
		},
		frequency: {
			type: String,
			default: null,
		},
		frequency_type: {
			type: String,
			default: null,
		},
		transaction_amount: {
			type: String,
			default: null,
		},
		currency_id: {
			type: String,
			default: null,
		},
		back_url: {
			type: String,
			default: null,
		},
		payer_email: {
			type: String,
			default: null,
		},
		start_date: {
			type: String,
			default: null,
		},
		end_date: {
			type: String,
			default: null,
		},
		next_payment_date: {
			type: String,
			default: null,
		},
		last_modified: {
			type: String,
			default: null,
		},
		status: {
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

export default Plan;
