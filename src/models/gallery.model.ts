import { model, Schema } from "mongoose";
import { GalleryModelType } from "../utils";

// Create a new Mongoose model for the gallery document
const Gallery = model(
	"gallery",
	// Define the schema for the gallery document
	new Schema<GalleryModelType>({
		imageId: {
			type: String,
			default: null,
		},
		deviceId: {
			type: String,
			required: true,
		},
		filepath: {
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

export default Gallery;
