const functions = require("firebase-functions");
const nodemailer = require("nodemailer");
const cors = require("cors")({ origin: true }); // Enable CORS for mobile access

// Create transporter with explicit configuration
const transporter = nodemailer.createTransport({
  host: 'smtp.gmail.com',
  port: 587,
  secure: false, // true for 465, false for other ports
  auth: {
    user: 'yashwanthbm36@gmail.com',
    pass: process.env.SMTP_PASSWORD, // This needs to be configured in Firebase console
  },
  tls: {
    rejectUnauthorized: false // For development, consider removing in production
  }
});

exports.sendEmail = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    if (req.method !== 'POST') {
      return res.status(405).send({ error: 'Method Not Allowed' });
    }

    try {
      const { recipients, subject, body, imageBase64 } = req.body;
      
      // Validate required fields
      if (!recipients || recipients.length === 0 || !subject || !body) {
        return res.status(400).send({ error: 'Missing required fields' });
      }

      // Log environment variable status (for debugging)
      console.log("SMTP Password exists:", !!process.env.SMTP_PASSWORD);
      
      const mailOptions = {
        from: 'yashwanthbm36@gmail.com',
        to: recipients.join(", "),
        subject,
        html: body,
      };

      // Add image attachment if provided
      if (imageBase64) {
        mailOptions.attachments = [
          {
            filename: 'detection_image.jpg',
            content: imageBase64,
            encoding: 'base64',
          },
        ];
      }

      // Send email
      await transporter.sendMail(mailOptions);
      console.log('✅ Email sent successfully');
      return res.status(200).send({ success: true });
    } catch (error) {
      console.error('❌ Error sending email:', error);
      return res.status(500).send({ 
        success: false, 
        error: error.message,
        stack: error.stack // Include stack trace for debugging
      });
    }
  });
});