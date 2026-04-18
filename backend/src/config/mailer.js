const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
  host: process.env.MAIL_HOST,
  port: Number(process.env.MAIL_PORT || 587),
  secure: Number(process.env.MAIL_PORT) === 465,
  auth: {
    user: process.env.MAIL_USER,
    pass: process.env.MAIL_PASS
  },
  tls: {
    rejectUnauthorized: false
  }
});

transporter.verify((error, success) => {
  if (error) {
    console.error("SMTP verify lỗi:", error);
  } else {
    console.log("SMTP sẵn sàng gửi mail");
  }
});

async function sendMail(to, subject, html) {
  if (!to) {
    throw new Error("Thiếu email người nhận");
  }

  const info = await transporter.sendMail({
    from: process.env.MAIL_FROM || `"Emotion Debugging" <${process.env.MAIL_USER}>`,
    to,
    subject,
    html
  });

  console.log("Send mail success:", info.response);
  return info;
}

module.exports = { sendMail };