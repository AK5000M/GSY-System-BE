// import { Response } from "express";
// import nodemailer from "nodemailer";
// import { MailBodyType } from "../../utils";

// const { NODE_ENV, EMAIL_USER, EMAIL_PASS } = process.env;

// const sendEmail = (
// 	res: Response, // Remove 'res' parameter here
// 	fromEmail: string,
// 	toEmail: string,
// 	subject: string,
// 	html: string,
// 	message?: string,
// 	token?: string
// ) => {
// 	try {
// 		if (NODE_ENV === "development") {
// 			return; // No need to send a response here
// 		}

// 		const transporter = nodemailer.createTransport({
// 			host: "smtp.gmail.com",
// 			service: "gmail",
// 			port: 587,
// 			secure: true,
// 			auth: {
// 				user: EMAIL_USER,
// 				pass: EMAIL_PASS,
// 			},
// 		});

// 		const body: MailBodyType = {
// 			from: fromEmail,
// 			to: toEmail,
// 			subject: subject,
// 			html: html, // Include newPassword in the HTML content
// 		};

// 		transporter.sendMail(body, (err, info) => {
// 			if (err) {
// 				console.log(err);
// 				return res.status(403).json({
// 					status: "error",
// 					message: `An error occurred. Please try again later.`,
// 					error: err.message,
// 				});
// 			} else {
// 				res.status(200).json({ message: "Password reset email sent" });
// 			}
// 		});
// 	} catch (err) {
// 		console.log(err);
// 		return res.status(500).json({ message: "Internal server error" });
// 	}
// };

// const mailer = {
// 	sendEmail,
// };

// export default mailer;

import nodemailer from "nodemailer";

// Nodemailer configuration
const transporter = nodemailer.createTransport({
	host: process.env.SMTP_HOST,
	port: Number(process.env.SMTP_PORT),
	secure: false,
	auth: {
		user: process.env.SMTP_USERNAME,
		pass: process.env.SMTP_PASSWORD,
	},
});

// Function to send email to the admin
export const sendAdminNotification = async (
	newUserEmail: string,
	newUserName: string
) => {
	try {
		const adminEmail = process.env.ADMIN_EMAIL;

		// Email content
		const mailOptions = {
			from: '"Your App Name" <no-reply@yourapp.com>',
			to: adminEmail,
			subject: "New User Registration",
			text: `A new user has registered with the email: ${newUserEmail} and username: ${newUserName}.`, // Plain text body
			html: `<p>A new user has registered:</p><ul><li>Email: ${newUserEmail}</li><li>Username: ${newUserName}</li></ul>`, // HTML body
		};
		console.log({ mailOptions });
		// Send the email
		await transporter.sendMail(mailOptions);
		console.log("Admin notification email sent.");
	} catch (error) {
		console.error("Error sending admin notification email:", error);
	}
};
