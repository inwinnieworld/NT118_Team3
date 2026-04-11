const crypto = require("crypto");
const pool = require("../config/db");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const { sendMail } = require("../config/mailer");

function mapRole(row) {
  if (row.student_id) return "STUDENT";
  if (row.admin_id) return "ADMIN";
  if (row.staff_id) return "STAFF";
  return "USER";
}

function buildBridgeResetLink(resetToken) {
  const baseUrl = process.env.RESET_BRIDGE_BASE_URL || "http://10.0.2.2:3000";
  return `${baseUrl}/api/auth/open-reset-password?token=${encodeURIComponent(resetToken)}`;
}

async function findUserByAccount(account) {
  const sql = `
    SELECT
      u.user_id,
      u.name,
      u.email,
      u.password_hash,
      u.phone,
      u.is_locked,
      s.student_id,
      s.student_code,
      a.admin_id,
      a.admin_role,
      st.staff_id,
      st.position
    FROM USERS u
    LEFT JOIN STUDENTS s ON s.user_id = u.user_id
    LEFT JOIN ADMINS a ON a.user_id = u.user_id
    LEFT JOIN STAFF st ON st.user_id = u.user_id
    WHERE u.email = ? OR s.student_code = ?
    LIMIT 1
  `;
  const [rows] = await pool.execute(sql, [account, account]);
  return rows[0] || null;
}

async function findUserByEmail(email) {
  const sql = `
    SELECT user_id, name, email, password_hash, is_locked
    FROM USERS
    WHERE email = ?
    LIMIT 1
  `;
  const [rows] = await pool.execute(sql, [email]);
  return rows[0] || null;
}

async function registerStudent({ name, email, password, phone, studentCode, major, faculty, yearOfStudy }) {
  const existingUser = await findUserByEmail(email);
  if (existingUser) {
    return { success: false, status: 409, message: "Email đã tồn tại" };
  }

  const [studentCodeRows] = await pool.execute(
    `SELECT student_id FROM STUDENTS WHERE student_code = ? LIMIT 1`,
    [studentCode]
  );

  if (studentCodeRows.length > 0) {
    return { success: false, status: 409, message: "Mã số sinh viên đã tồn tại" };
  }

  const hashedPassword = await bcrypt.hash(password, 10);

  const connection = await pool.getConnection();
  try {
    await connection.beginTransaction();

    const [userResult] = await connection.execute(
      `
      INSERT INTO USERS (name, email, password_hash, phone, is_locked)
      VALUES (?, ?, ?, ?, FALSE)
      `,
      [name, email, hashedPassword, phone || null]
    );

    const userId = userResult.insertId;

    await connection.execute(
      `
      INSERT INTO STUDENTS (user_id, student_code, major, faculty, year_of_study)
      VALUES (?, ?, ?, ?, ?)
      `,
      [userId, studentCode, major || null, faculty || null, yearOfStudy || null]
    );

    await connection.commit();

    return {
      success: true,
      status: 201,
      message: "Đăng ký tài khoản sinh viên thành công"
    };
  } catch (error) {
    await connection.rollback();
    throw error;
  } finally {
    connection.release();
  }
}

async function login({ account, password }) {
  const user = await findUserByAccount(account);

  if (!user) {
    return { success: false, status: 401, message: "Tài khoản không tồn tại" };
  }

  if (user.is_locked) {
    return { success: false, status: 403, message: "Tài khoản đã bị khóa" };
  }

  const isMatch = await bcrypt.compare(password, user.password_hash).catch(() => false);

  if (!isMatch) {
    return { success: false, status: 401, message: "Sai mật khẩu" };
  }

  const role = mapRole(user);

  const token = jwt.sign(
    {
      userId: user.user_id,
      email: user.email,
      role
    },
    process.env.JWT_SECRET,
    { expiresIn: "7d" }
  );

  return {
    success: true,
    status: 200,
    message: "Đăng nhập thành công",
    data: {
      token,
      user: {
        userId: user.user_id,
        name: user.name,
        email: user.email,
        phone: user.phone,
        role,
        studentId: user.student_id || null,
        studentCode: user.student_code || null,
        adminRole: user.admin_role || null,
        staffPosition: user.position || null
      }
    }
  };
}

async function forgotPasswordRequest({ email }) {
  const user = await findUserByEmail(email);

  if (!user) {
    return {
      success: true,
      status: 200,
      message: "Nếu email tồn tại trong hệ thống, liên kết đặt lại mật khẩu đã được gửi."
    };
  }

  const resetToken = crypto.randomBytes(32).toString("hex");

  await pool.execute(
    `
    UPDATE PASSWORD_RESET_TOKENS
    SET is_used = TRUE
    WHERE user_id = ? AND is_used = FALSE
    `,
    [user.user_id]
  );

  await pool.execute(
    `
    INSERT INTO PASSWORD_RESET_TOKENS (user_id, reset_token, expires_at, is_used)
    VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 10 MINUTE), FALSE)
    `,
    [user.user_id, resetToken]
  );

  const appDeepLink = `emotedebugging://reset-password?token=${resetToken}`;
  const resetLink = `http://10.0.2.2:3000/api/auth/open-reset-password?token=${resetToken}`;

  if (process.env.MAIL_USER) {
    await sendMail(
      user.email,
      "Đặt lại mật khẩu - Emote Debugging",
      `
        <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #1f2937;">
          <h3>Emote Debugging - Quên mật khẩu</h3>
          <p>Xin chào ${user.name},</p>
          <p>Bạn đã yêu cầu đặt lại mật khẩu.</p>
          <p>Nhấn vào nút dưới đây để mở ứng dụng và tạo mật khẩu mới:</p>

          <p style="margin: 20px 0;">
            <a href="${resetLink}" style="
              display:inline-block;
              padding:12px 20px;
              background:#20b8d9;
              color:#ffffff;
              text-decoration:none;
              border-radius:8px;
              font-weight:bold;
            ">
              Đặt lại mật khẩu
            </a>
          </p>

          <p>Liên kết có hiệu lực trong 10 phút.</p>
          <p>Nếu nút không hoạt động, hãy mở liên kết web sau:</p>
          <p>${resetLink}</p>

          <p>Hoặc mở trực tiếp ứng dụng bằng liên kết sau:</p>
          <p>${appDeepLink}</p>
        </div>
      `
    );
  }

  return {
    success: true,
    status: 200,
    message: "Nếu email tồn tại trong hệ thống, liên kết đặt lại mật khẩu đã được gửi."
  };
}

async function validateResetToken({ token }) {
  const [rows] = await pool.execute(
    `
    SELECT prt.reset_id, prt.user_id, prt.is_used, prt.expires_at, u.email
    FROM PASSWORD_RESET_TOKENS prt
    JOIN USERS u ON u.user_id = prt.user_id
    WHERE prt.reset_token = ?
    LIMIT 1
    `,
    [token]
  );

  if (!rows.length) {
    return { success: false, status: 400, message: "Liên kết không hợp lệ" };
  }

  const record = rows[0];

  if (record.is_used) {
    return { success: false, status: 400, message: "Liên kết đã được sử dụng" };
  }

  if (new Date(record.expires_at) < new Date()) {
    return { success: false, status: 400, message: "Liên kết đã hết hạn" };
  }

  return {
    success: true,
    status: 200,
    message: "Liên kết hợp lệ",
    data: {
      email: record.email
    }
  };
}

async function resetPassword({ token, newPassword }) {
  const [rows] = await pool.execute(
    `
    SELECT reset_id, user_id, is_used, expires_at
    FROM PASSWORD_RESET_TOKENS
    WHERE reset_token = ?
    LIMIT 1
    `,
    [token]
  );

  if (!rows.length) {
    return { success: false, status: 400, message: "Liên kết không hợp lệ" };
  }

  const record = rows[0];

  if (record.is_used) {
    return { success: false, status: 400, message: "Liên kết đã được sử dụng" };
  }

  if (new Date(record.expires_at) < new Date()) {
    return { success: false, status: 400, message: "Liên kết đã hết hạn" };
  }

  const hashedPassword = await bcrypt.hash(newPassword, 10);

  const connection = await pool.getConnection();
  try {
    await connection.beginTransaction();

    await connection.execute(
      `
      UPDATE USERS
      SET password_hash = ?
      WHERE user_id = ?
      `,
      [hashedPassword, record.user_id]
    );

    await connection.execute(
      `
      UPDATE PASSWORD_RESET_TOKENS
      SET is_used = TRUE
      WHERE reset_id = ?
      `,
      [record.reset_id]
    );

    await connection.commit();

    return {
      success: true,
      status: 200,
      message: "Đặt lại mật khẩu thành công"
    };
  } catch (error) {
    await connection.rollback();
    throw error;
  } finally {
    connection.release();
  }
}

module.exports = {
  registerStudent,
  login,
  forgotPasswordRequest,
  validateResetToken,
  resetPassword
};