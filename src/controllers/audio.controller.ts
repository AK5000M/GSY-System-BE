import Audio from "../models/keylogs.model";
import { AudioModelType } from "../utils";

// Add New Audio

export const addNewAudio = async (data: any) => {
	try {
		const { deviceId, audio } = data;

		const newAudio: AudioModelType = new Audio({
			deviceId,
			audio,
		});

		await newAudio.save();

		return { status: 200, message: "Audio added successfully" };
	} catch (error) {
		console.error("Error adding audio:", error);
		return { status: 500, error: "Failed to add audio" };
	}
};
