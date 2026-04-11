const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
  host: process.env.MAIL_HOST,
  port: Number(process.env.MAIL_PORT || 587),
  secure: false,
  auth: {
    user: process.env.MAIL_USER,
    pass: process.env.MAIL_PASS
  },
  tls: {
    rejectUnauthorized: false
  }
});

async function sendMail(to, subject, html) {
  if (!to) {
    throw new Error("Thiếu email người nhận");
  }

  return transporter.sendMail({
    from: process.env.MAIL_FROM || `"Emotion Debugging" <${process.env.MAIL_USER}>`,
    to,
    subject,
    html
  });
}

module.exports = { sendMail };