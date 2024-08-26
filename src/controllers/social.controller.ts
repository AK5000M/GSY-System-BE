import { Request, Response } from 'express';
import { validationResult } from 'express-validator';

import Social from '../models/social.model';
import WhatsappClientList from '../models/whatsappClientList.model';
import WhatsappClientMessage from '../models/whatsappClientMessage.model';
import WhatsappProfile from '../models/whatsappProfile.model';
import InstagramClientList from '../models/instagramClientList.model';
import InstagramClientMessage from '../models/instagramClientMessage.model';

import {
  SocialModelType,
  SocialListModelType,
  SocialMessageModelType,
  SocialProfileModelType,
} from '../utils';

// Add New Social
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const addNewSocial = async (req: Request, res: Response) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  try {
    const { deviceId, userId, socialName, socialLink } = req.body;

    // Create a new social entry
    const newSocial: SocialModelType = new Social({
      deviceId,
      userId,
      socialName,
      socialLink,
    });

    // Save the new social entry to the database
    await newSocial.save();

    res.status(201).json({
      message: 'New social entry added successfully',
      social: newSocial,
    });
  } catch (error) {
    console.error('Error adding new social:', error);
    res.status(500).json({ error: 'Failed to add new social entry' });
  }
};

// Get Social List
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const getSocialList = async (req: Request, res: Response) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  try {
    const { deviceId, userId } = req.body;

    // Query the database to find social entries based on deviceId and userId
    const socialList = await Social.find({
      deviceId,
      userId,
    });

    res.status(200).json({ data: socialList });
  } catch (error) {
    console.error('Error getting social list:', error);
    res.status(500).json({ error: 'Failed to get social list' });
  }
};

//Filter Social List
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const filterSocialList = async (req: Request, res: Response) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  try {
    const { deviceId, socialName } = req.body;

    // Query the database to find social entries based on the filter
    const socialList = await Social.find({
      deviceId,
      socialName,
    });

    res.status(200).json({ data: socialList });
  } catch (error) {
    console.error('Error getting social list:', error);
    res.status(500).json({ error: 'Failed to get social list' });
  }
};

// Delete One Social
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteOneSocial = async (req: Request, res: Response) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  try {
    const { socialId } = req.body;

    // Query the database to find social entries based on the delete
    const deleteSocial = await Social.findOneAndDelete({
      _id: socialId,
    });

    if (!deleteSocial) {
      return res.status(404).json({ error: 'Social data not found' });
    }

    res.status(200).json({
      message: 'Social data deleted successfully',
    });
  } catch (error) {
    console.error('Error delete social list:', error);
    res.status(500).json({ error: 'Failed to delete social list' });
  }
};

// Delete All Social Lists
/**
 *
 * @param {*} req
 * @param {*} res
 */
export const deleteAllSocials = async (req: Request, res: Response) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  try {
    const { deviceId } = req.body;

    // Query the database to find social entries based on the delete
    const deleteSocial = await Social.findOneAndDelete({
      deviceId: deviceId,
    });

    if (!deleteSocial) {
      return res.status(404).json({ error: 'Social data not found' });
    }

    res.status(200).json({
      message: 'Social data deleted successfully',
    });
  } catch (error) {
    console.error('Error delete social list:', error);
    res.status(500).json({ error: 'Failed to delete social list' });
  }
};

// ############## Social Managers for Socket server ################
// ********************* Whatsapp ************************* /
// Add Whatsapp Client List
export const addWhatsappClientList = async (deviceId: string, data: any) => {
  try {
    for (const item of data) {
      // Extract relevant fields from item
      const { socialName, phoneNumber, userName, lastMessage, lastDate } = item;

      // Check if a client with the same deviceId and phoneNumber already exists
      const existingClient = await WhatsappClientList.findOne({
        deviceId,
        phoneNumber,
        socialName,
      });

      if (existingClient) {
        console.log('Client already exists:', existingClient?.deviceId);

        // Update lastMessage and lastDate of existing client
        existingClient.lastMessage = lastMessage;
        existingClient.lastDate = lastDate;

        // Save the updated document to the database
        await existingClient.save();

        continue; // Skip to the next item after updating
      } else {
        // Create a new document using the SocialClientList model
        const newClient: SocialListModelType = new WhatsappClientList({
          deviceId,
          socialName,
          phoneNumber,
          userName,
          lastMessage,
          lastDate,
        });

        // Save the new document to the database
        await newClient.save();
      }
    }

    // Return success status
    return { status: 200, message: 'Client list processed successfully' };
  } catch (error) {
    console.error('Error adding whatsapp client list:', error);

    // Return error status
    return { status: 500, error: 'Failed to add whatsapp client list' };
  }
};

// Add Whatsapp Clients Message
export const addWhatsappClientMessage = async (
  deviceId: string,
  data: any[]
) => {
  try {
    for (const item of data) {
      // Extract relevant fields from item
      const {
        socialName,
        phoneNumber,
        userName,
        messageText,
        referenceType,
        messageDate,
        messageTime,
      } = item;

      // Check if a message with the same deviceId, phoneNumber, messageText, and messageTime already exists
      const existingMessage = await WhatsappClientMessage.findOne({
        deviceId,
        socialName,
        phoneNumber,
        userName,
        messageText,
        messageDate,
        messageTime,
      });

      if (existingMessage) {
        console.log('Message already exists:', existingMessage?.deviceId);
        continue; // Skip to the next item if the message already exists
      } else {
        // Create a new document using the WhatsappClientMessage model
        const newMessage: SocialMessageModelType = new WhatsappClientMessage({
          deviceId,
          socialName,
          phoneNumber,
          userName,
          messageText,
          referenceType,
          messageDate,
          messageTime,
        });
        console.log('new message', newMessage);

        // Save the new document to the database
        await newMessage.save();
      }
    }

    // Return success status
    return {
      status: 200,
      message: 'Client messages processed successfully',
    };
  } catch (error) {
    console.error('Error adding whatsapp client message:', error);

    // Return error status
    return { status: 500, error: 'Failed to add whatsapp client message' };
  }
};

// Add Whatsapp Profile
export const addWhatsappProfile = async (data: any) => {
  try {
    // Extract relevant fields from data
    const { deviceId, socialName, userName, phoneNumber, stateText } = data;

    // Create a new document using the SocialProfile model
    const newProfile: SocialProfileModelType = new WhatsappProfile({
      deviceId,
      socialName,
      userName,
      phoneNumber,
      stateText,
    });

    // Save the new document to the database
    await newProfile.save();

    console.log('Profile added successfully:', newProfile);

    // Return success status
    return { status: 200, data: newProfile };
  } catch (error) {
    console.error('Error registering whatsapp profile:', error);

    // Return error status
    return { status: 500, error: 'Failed to register whatsapp profile' };
  }
};

// Get Whatsapp Client List
export const getWhatsappClientList = async (req: Request, res: Response) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  try {
    const { deviceId, social } = req.params;

    // Query the database to find social entries based on deviceId
    const socialList = await WhatsappClientList.find({
      deviceId,
      socialName: social,
    }).sort({ created_at: -1 });

    if (!socialList.length) {
      return res.status(404).json({
        message: 'No clients found for this deviceId and social',
      });
    }

    res.status(200).json({ data: socialList });
  } catch (error) {
    console.error('Error getting social list:', error);
    res.status(500).json({ error: 'Failed to get social list' });
  }
};

// Get Whatsapp Client Message
export const getWhatsappClientMessage = async (req: Request, res: Response) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  try {
    const { deviceId, social, identifier } = req.params;

    // Query the database to find the client message based on deviceId, social, and either userName or phoneNumber
    const clientMessage = await WhatsappClientMessage.find({
      deviceId,
      socialName: social,
      $or: [{ userName: identifier }, { phoneNumber: identifier }],
    });

    if (!clientMessage) {
      return res.status(404).json({
        message: 'No client message found for this deviceId and identifier',
      });
    }

    res.status(200).json({ data: clientMessage });
  } catch (error) {
    console.error('Error getting client message:', error);
    res.status(500).json({ error: 'Failed to get client message' });
  }
};

//**************** Instagram *********************/
// Add Instagram Client List
export const addInstagramClientList = async (deviceId: string, data: any) => {
  try {
    for (const item of data) {
      // Extract relevant fields from item
      const { socialName, userName, lastMessage, lastDate } = item;

      // Check if a client with the same deviceId and userName already exists
      const existingClient = await InstagramClientList.findOne({
        deviceId,
        userName,
        socialName,
      });

      if (existingClient) {
        // Update lastMessage and lastDate of existing client
        existingClient.lastMessage = lastMessage;
        existingClient.lastDate = lastDate;

        // Save the updated document to the database
        await existingClient.save();
        continue; // Skip to the next item if the client already exists
      } else {
        // Create a new document using the SocialClientList model
        const newClient: SocialListModelType = new InstagramClientList({
          deviceId,
          socialName,
          userName,
          lastMessage,
          lastDate,
        });

        // Save the new document to the database
        await newClient.save();

        console.log('Client list added successfully:', newClient);
      }
    }

    // Return success status
    return { status: 200, message: 'Client list processed successfully' };
  } catch (error) {
    console.error('Error adding instagram client list:', error);

    // Return error status
    return { status: 500, error: 'Failed to add instagram client list' };
  }
};

// Add Instagram Clients Message
export const addInstagramClientMessage = async (
  deviceId: string,
  data: any[]
) => {
  try {
    for (const item of data) {
      // Extract relevant fields from item
      const { socialName, userName, messageText, referenceType, messageTime } =
        item;

      // Check if a message with the same deviceId, userName, messageText, and messageTime already exists
      const existingMessage = await InstagramClientMessage.findOne({
        deviceId,
        socialName,
        userName,
        messageText,
        messageTime,
      });

      if (existingMessage) {
        // console.log('Message already exists:', existingMessage?.deviceId);
        continue; // Skip to the next item if the message already exists
      } else {
        // Create a new document using the InstagramClientMessage model
        const newMessage: SocialMessageModelType = new InstagramClientMessage({
          deviceId,
          socialName,
          userName,
          messageText,
          referenceType, 
          messageTime,
        });
        console.log('new message', newMessage);

        // Save the new document to the database
        await newMessage.save();

        console.log('Client message added successfully:', newMessage);
      }
    }

    // Return success status
    return {
      status: 200,
      message: 'Client messages processed successfully',
    };
  } catch (error) {
    console.error('Error adding whatsapp client message:', error);

    // Return error status
    return { status: 500, error: 'Failed to add whatsapp client message' };
  }
};

// Get Instagam Client List
export const getInstagramClientList = async (req: Request, res: Response) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  try {
    const { deviceId, social } = req.params;

    // Query the database to find social entries based on deviceId
    const socialList = await InstagramClientList.find({
      deviceId,
      socialName: social,
    }).sort({ created_at: 1 });

    if (!socialList.length) {
      return res.status(404).json({
        message: 'No clients found for this deviceId and social',
      });
    }

    res.status(200).json({ data: socialList });
  } catch (error) {
    console.error('Error getting social list:', error);
    res.status(500).json({ error: 'Failed to get social list' });
  }
};

// Get Instagam Client Message
export const getInstagramClientMessage = async (
  req: Request,
  res: Response
) => {
  // Check for validation errors
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  try {
    const { deviceId, social, username } = req.params;

    // Query the database to find the client message based on deviceId, social, and either userName
    const clientMessage = await InstagramClientMessage.find({
      deviceId,
      socialName: social,
      userName: username,
    }).sort({ created_at: 1 });

    if (!clientMessage) {
      return res.status(404).json({
        message: 'No client message found for this deviceId and username',
      });
    }

    res.status(200).json({ data: clientMessage });
  } catch (error) {
    console.error('Error getting client message:', error);
    res.status(500).json({ error: 'Failed to get client message' });
  }
};
