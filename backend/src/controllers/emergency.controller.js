const emergencyService = require("../services/emergency.service");
const { ok, fail } = require("../utils/response");

async function getResources(req, res) {
  try {
    const data = await emergencyService.getResources();
    return ok(res, data, "Lấy danh sách hotfix thành công");
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getContactsByStudentId(req, res) {
  try {
    const { studentId } = req.params;
    const data = await emergencyService.getContactsByStudentId(studentId);
    return ok(res, data, "Lấy danh bạ khẩn cấp thành công");
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function createContact(req, res) {
  try {
    const { studentId, contactName, phone, relationship } = req.body;

    if (!studentId || !contactName || !phone) {
      return fail(res, "Thiếu studentId, contactName hoặc phone", 400);
    }

    const data = await emergencyService.createContact({
      studentId,
      contactName,
      phone,
      relationship
    });

    return ok(res, data, "Tạo liên hệ khẩn cấp thành công", 201);
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function updateContact(req, res) {
  try {
    const { contactId } = req.params;
    const { contactName, phone, relationship } = req.body;

    if (!contactName || !phone) {
      return fail(res, "Thiếu contactName hoặc phone", 400);
    }

    await emergencyService.updateContact(contactId, {
      contactName,
      phone,
      relationship
    });

    return ok(res, null, "Cập nhật liên hệ thành công");
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function deleteContact(req, res) {
  try {
    const { contactId } = req.params;
    await emergencyService.deleteContact(contactId);
    return ok(res, null, "Xóa liên hệ thành công");
  } catch (error) {
    return fail(res, "Lỗi server", 500, error.message);
  }
}

module.exports = {
  getResources,
  getContactsByStudentId,
  createContact,
  updateContact,
  deleteContact
};