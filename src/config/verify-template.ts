/**
 *  Reset Password Template
 *
 * @param newPassword
 * @returns
 */

const resetPasswordTemplate = (newPassword: string): string => {
	return `<div style="font-family:Arial, Helvetica, sans-serif; max-width: 800px; margin:auto; padding:20px 40px 20px 40px;">
                <div style="font-size:30px; font-weight: 600;">
                    <img style="float: left; margin-right: 10px;" width="50px" src="LOGO" alt="" />
                    <div>
                        <div style="font-family:Verdana, Geneva, Tahoma, sans-serif;
                        font-size:28px;
                        font-style:normal;
                        font-weight:600;
                        line-height:115%; /* 37.8px */
                        letter-spacing:0.72px;
                        color:#4A25E1">
                            TechDroid
                        </div>
                    </div>
                </div>
                
                <h2 style="text-align:center; padding:20px 0px 20px 0px;">  
                    Thank you for reaching out to us.
                </h2>
                <div style="line-height:1.5rem;">
                    <p>
                        We have received a request to change the password for your TechDroid account. To reset your password, please use the following temporary password:
                    </p>
                    <p style="margin: 10px 0px">
                        Temporary Password: <b style="font-size: 20px;">${newPassword}</b>
                    </p>
                    <p>
                        Please note that this password will expire in 15 minutes. We recommend resetting your password as soon as possible to ensure the security of your account.
                    </p>
                    <p>
                        If you did not request this password change or have any concerns, please contact our support team at 
                        <a href="mailto:support@TechDroid.com">support@TechDroid.com</a> for further assistance.
                    </p>
                    <p>
                        Best regards,
                        TechDroid Team
                    </p>
                </div>
                <div style="padding: 10px 20px 10px 20px; text-align: center;">
                    Copyright ©2023 TechDroid
                </div>
            </div> `;
};

/**
 *  Updated Password Template
 *
 * @param
 * @returns
 */

const updatedPasswordTemplate = (): string => {
	return `<div style="font-family:Arial, Helvetica, sans-serif; max-width: 800px; margin:auto; padding:20px 40px 20px 40px;">
                <div style="font-size:30px; font-weight: 600;">
                    <img style="float: left; margin-right: 10px;" width="50px" src="LOGO" alt="" />
                    <div>
                        <div style="font-family:Verdana, Geneva, Tahoma, sans-serif;
                        font-size:28px;
                        font-style:normal;
                        font-weight:600;
                        line-height:115%; /* 37.8px */
                        letter-spacing:0.72px;
                        color:#4A25E1">
                            TechDroid
                        </div>
                    </div>
                </div>
                
                <h2 style="text-align:center; padding:20px 0px 20px 0px;">  
                    Thank you for reaching out to us.
                </h2>
                <div style="line-height:1.5rem;">
                    <p>
                        We have successfully updated your TechDroid account password.
                    </p>
                    <p>
                        If you did not request this password change or have any concerns, please contact our support team at 
                        <a href="mailto:support@TechDroid.com">support@TechDroid.com</a> for further assistance.
                    </p>
                    <p>
                        Best regards,
                        TechDroid Team
                    </p>
                </div>
                <div style="padding: 10px 20px 10px 20px; text-align: center;">
                    Copyright ©2023 TechDroid
                </div>
            </div> `;
};

const template = {
	resetPasswordTemplate,
	updatedPasswordTemplate,
};

export default template;
