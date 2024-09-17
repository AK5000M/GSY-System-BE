import { model, Schema } from "mongoose";
import { SessionModelType } from "../utils";

const Session = model(
	"session",
	new Schema<SessionModelType>({
		userId: { type: String, required: true },
		token: { type: String, required: true },
		created_at: { type: Date, default: Date.now },
	})
);

export default Session;
