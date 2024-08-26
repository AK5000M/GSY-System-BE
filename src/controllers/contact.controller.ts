import { Request, Response } from "express";
import { validationResult } from "express-validator";
import Contact from "../models/contact.model"; // Assuming this is your Mongoose model
import { ContactModelType } from "../utils"; // Assuming you have FileModelType defined

// New Contacts
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const addNewContact = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { fullName, email, subject, message } = req.body;

		// Create a new contact using Mongoose model
		const newContact: ContactModelType = new Contact({
			fullName,
			email,
			subject,
			message,
		});

		// Save the new contact document to the database
		await newContact.save();

		return res.status(200).json({
			status: "200",
			message: "File saved successfully",
		});
	} catch (error) {
		console.error("Error uploading contact:", error);
		return res.status(500).json({
			status: "500",
			error: "Failed to save file to the contact",
		});
	}
};

// Get Contacts
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getContacts = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}
	try {
		const { email } = req.body;

		// Find contacts in the database that match the query
		const contacts: ContactModelType[] = await Contact.find({
			email: email,
		});

		res.status(200).json({ success: true, contacts });
	} catch (error) {
		console.error("Error fetching contacts:", error);
		res.status(500).json({ error: "Failed to fetch contacts" });
	}
};
