const authService = require("../services/auth.service");
const { ok, fail } = require("../utils/response");

async function register(req, res) {
  try {
    const { name, email, password, phone, studentCode, major, faculty, yearOfStudy } = req.body;

    if (!name || !email || !password || !studentCode) {
      return fail(res, "Thiếu name, email, password hoặc studentCode", 400);
    }

    const result = await authService.registerStudent({
      name,
      email,
      password,
      phone,
      studentCode,
      major,
      faculty,
      yearOfStudy
    });

    if (!result.success) {
      return fail(res, result.message, result.status);
    }

    return ok(res, null, result.message, result.status);
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function login(req, res) {
  try {
    const { account, password } = req.body;

    if (!account || !password) {
      return fail(res, "Thiếu account hoặc password", 400);
    }

    const result = await authService.login({ account, password });

    if (!result.success) {
      return fail(res, result.message, result.status);
    }

    return ok(res, result.data, result.message, result.status);
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function forgotPasswordRequest(req, res) {
  try {
    const { email } = req.body;

    if (!email) {
      return fail(res, "Thiếu email", 400);
    }

    const result = await authService.forgotPasswordRequest({ email });

    if (!result.success) {
      return fail(res, result.message, result.status);
    }

    return ok(res, null, result.message, result.status);
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function validateResetToken(req, res) {
  try {
    const { token } = req.body;

    if (!token) {
      return fail(res, "Thiếu token", 400);
    }

    const result = await authService.validateResetToken({ token });

    if (!result.success) {
      return fail(res, result.message, result.status);
    }

    return ok(res, result.data || null, result.message, result.status);
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function resetPassword(req, res) {
  try {
    const { token, newPassword } = req.body;

    if (!token || !newPassword) {
      return fail(res, "Thiếu token hoặc newPassword", 400);
    }

    const result = await authService.resetPassword({ token, newPassword });

    if (!result.success) {
      return fail(res, result.message, result.status);
    }

    return ok(res, null, result.message, result.status);
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function openResetPasswordPage(req, res) {
  try {
    const { token } = req.query;

    if (!token) {
      return res.status(400).send("Thiếu token");
    }

    const appLink = `emotedebugging://reset-password?token=${encodeURIComponent(token)}`;

    return res.status(200).send(`
      <!DOCTYPE html>
      <html lang="vi">
      <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Mở Emote Debugging</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            background: #f5f7fb;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
            padding: 24px;
            box-sizing: border-box;
          }
          .card {
            max-width: 420px;
            width: 100%;
            background: #ffffff;
            border-radius: 16px;
            padding: 28px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
            text-align: center;
          }
          h2 {
            margin-top: 0;
            color: #0f172a;
          }
          p {
            color: #475569;
            line-height: 1.6;
          }
          .btn {
            display: inline-block;
            margin-top: 16px;
            padding: 12px 20px;
            background: #20b8d9;
            color: white;
            text-decoration: none;
            border-radius: 10px;
            font-weight: bold;
          }
          .link {
            margin-top: 16px;
            word-break: break-all;
            font-size: 13px;
            color: #64748b;
          }
        </style>
      </head>
      <body>
        <div class="card">
          <h2>Đang mở ứng dụng...</h2>
          <p>Nếu ứng dụng Emote Debugging đã được cài, hệ thống sẽ mở màn đặt lại mật khẩu.</p>
          <a class="btn" href="${appLink}">Mở ứng dụng</a>
          <p class="link">${appLink}</p>
        </div>

        <script>
          setTimeout(function () {
            window.location.href = "${appLink}";
          }, 300);
        </script>
      </body>
      </html>
    `);
  } catch (error) {
    return res.status(500).send("Lỗi server");
  }
}

module.exports = {
  register,
  login,
  forgotPasswordRequest,
  validateResetToken,
  resetPassword,
  openResetPasswordPage
};