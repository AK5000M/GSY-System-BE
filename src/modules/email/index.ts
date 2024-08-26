import { Response } from "express";
import nodemailer from "nodemailer";
import { MailBodyType } from "../../utils";

const { NODE_ENV, EMAIL_USER, EMAIL_PASS } = process.env;

const sendEmail = (
	res: Response, // Remove 'res' parameter here
	fromEmail: string,
	toEmail: string,
	subject: string,
	html: string,
	message?: string,
	token?: string
) => {
	try {
		if (NODE_ENV === "development") {
			return; // No need to send a response here
		}

		const transporter = nodemailer.createTransport({
			host: "smtp.gmail.com",
			service: "gmail",
			port: 587,
			secure: true,
			auth: {
				user: EMAIL_USER,
				pass: EMAIL_PASS,
			},
		});

		const body: MailBodyType = {
			from: fromEmail,
			to: toEmail,
			subject: subject,
			html: html, // Include newPassword in the HTML content
		};

		transporter.sendMail(body, (err, info) => {
			if (err) {
				console.log(err);
				return res.status(403).json({
					status: "error",
					message: `An error occurred. Please try again later.`,
					error: err.message,
				});
			} else {
				res.status(200).json({ message: "Password reset email sent" });
			}
		});
	} catch (err) {
		console.log(err);
		return res.status(500).json({ message: "Internal server error" });
	}
};

const mailer = {
	sendEmail,
};

export default mailer;
