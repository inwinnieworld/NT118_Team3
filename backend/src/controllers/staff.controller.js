const staffService = require("../services/staff.service");

function ok(res, data = null, message = "Thành công", status = 200) {
  return res.status(status).json({
    success: true,
    message,
    data,
    errors: null
  });
}

function fail(res, message = "Thất bại", status = 400, errors = null) {
  return res.status(status).json({
    success: false,
    message,
    data: null,
    errors
  });
}

async function createQuest(req, res) {
  try {
    const { errorTypeId, questTitle, questDescription } = req.body;

    if (!errorTypeId || !questTitle || !questTitle.trim()) {
      return fail(res, "Thiếu thông tin tạo quest", 400);
    }

    const quest = await staffService.createQuest({
      errorTypeId,
      questTitle: questTitle.trim(),
      questDescription: questDescription ? questDescription.trim() : ""
    });

    return ok(res, quest, "Tạo quest thành công", 201);
  } catch (error) {
    console.error("createQuest error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function updateQuest(req, res) {
  try {
    const { questId } = req.params;
    const { errorTypeId, questTitle, questDescription } = req.body;

    if (!questId) {
      return fail(res, "Thiếu questId", 400);
    }

    if (!errorTypeId || !questTitle || !questTitle.trim()) {
      return fail(res, "Thiếu thông tin cập nhật quest", 400);
    }

    const updated = await staffService.updateQuest(questId, {
      errorTypeId,
      questTitle: questTitle.trim(),
      questDescription: questDescription ? questDescription.trim() : ""
    });

    if (!updated) {
      return fail(res, "Không tìm thấy quest", 404);
    }

    return ok(res, null, "Cập nhật quest thành công", 200);
  } catch (error) {
    console.error("updateQuest error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getAllQuests(req, res) {
  try {
    const data = await staffService.getAllQuests();
    return ok(res, data, "Lấy danh sách quest thành công", 200);
  } catch (error) {
    console.error("getAllQuests error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function deleteQuest(req, res) {
  try {
    const { questId } = req.params;

    if (!questId) {
      return fail(res, "Thiếu questId", 400);
    }

    const result = await staffService.deleteQuest(questId);

    if (!result.success) {
      return fail(res, result.message, result.status);
    }

    return ok(res, null, result.message, result.status);
  } catch (error) {
    console.error("deleteQuest error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function assignQuestToStudent(req, res) {
  try {
    const { studentId, questId, errorLogId, status } = req.body;

    if (!studentId || !questId || !errorLogId) {
      return fail(res, "Thiếu thông tin gán quest", 400);
    }

    const assignment = await staffService.assignQuestToStudent({
      studentId,
      questId,
      errorLogId,
      status
    });

    return ok(res, assignment, "Gán quest thành công", 201);
  } catch (error) {
    console.error("assignQuestToStudent error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getQuestAssignments(req, res) {
  try {
    const data = await staffService.getQuestAssignments();
    return ok(res, data, "Lấy danh sách phân công thành công", 200);
  } catch (error) {
    console.error("getQuestAssignments error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getSummaryReport(req, res) {
  try {
    const data = await staffService.getSummaryReport();
    return ok(res, data, "Lấy báo cáo tổng quan thành công", 200);
  } catch (error) {
    console.error("getSummaryReport error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getErrorReport(req, res) {
  try {
    const data = await staffService.getErrorReport();
    return ok(res, data, "Lấy báo cáo lỗi thành công", 200);
  } catch (error) {
    console.error("getErrorReport error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getQuestReport(req, res) {
  try {
    const data = await staffService.getQuestReport();
    return ok(res, data, "Lấy báo cáo quest thành công", 200);
  } catch (error) {
    console.error("getQuestReport error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

module.exports = {
  createQuest,
  updateQuest,
  getAllQuests,
  deleteQuest,
  assignQuestToStudent,
  getQuestAssignments,
  getSummaryReport,
  getErrorReport,
  getQuestReport
};