import { model, Schema } from "mongoose";
import { AudioModelType } from "../utils";

const Audio = model(
	"audio",
	new Schema<AudioModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		audio: {
			type: String,
			required: true,
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
	})
);

export default Audio;
