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

/* =========================
   QUEST
========================= */
async function createQuest(req, res) {
  try {
    const { problemId, questTitle, questDescription } = req.body;

    if (!problemId || !questTitle || !questTitle.trim()) {
      return fail(res, "Thiếu thông tin tạo quest", 400);
    }

    const quest = await staffService.createQuest({
      problemId,
      questTitle: questTitle.trim(),
      questDescription: questDescription ? questDescription.trim() : ""
    });

    return ok(res, quest, "Tạo quest thành công", 201);
  } catch (error) {
    console.error("createQuest error:", error);
    return fail(res, "Lỗi server", error.status || 500, error.message);
  }
}

async function updateQuest(req, res) {
  try {
    const { questId } = req.params;
    const { problemId, questTitle, questDescription } = req.body;

    if (!questId) {
      return fail(res, "Thiếu questId", 400);
    }

    if (!problemId || !questTitle || !questTitle.trim()) {
      return fail(res, "Thiếu thông tin cập nhật quest", 400);
    }

    const updated = await staffService.updateQuest(questId, {
      problemId,
      questTitle: questTitle.trim(),
      questDescription: questDescription ? questDescription.trim() : ""
    });

    if (!updated) {
      return fail(res, "Không tìm thấy quest", 404);
    }

    return ok(res, null, "Cập nhật quest thành công", 200);
  } catch (error) {
    console.error("updateQuest error:", error);
    return fail(res, "Lỗi server", error.status || 500, error.message);
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

/* =========================
   ASSIGNMENT
========================= */
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

/* =========================
   REPORT
========================= */
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

async function getQuestTrendReport(req, res) {
  try {
    const data = await staffService.getQuestTrendReport();
    return ok(res, data, "Lấy dữ liệu biểu đồ quest thành công", 200);
  } catch (error) {
    console.error("getQuestTrendReport error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

/* =========================
   TRACE QUESTIONS
========================= */
async function getAllTraceQuestions(req, res) {
  try {
    const data = await staffService.getAllTraceQuestions();
    return ok(res, data, "Lấy danh sách câu hỏi trace thành công", 200);
  } catch (error) {
    console.error("getAllTraceQuestions error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getTraceQuestionDetail(req, res) {
  try {
    const { questionId } = req.params;

    if (!questionId) {
      return fail(res, "Thiếu questionId", 400);
    }

    const data = await staffService.getTraceQuestionDetail(questionId);

    if (!data) {
      return fail(res, "Không tìm thấy câu hỏi", 404);
    }

    return ok(res, data, "Lấy chi tiết câu hỏi thành công", 200);
  } catch (error) {
    console.error("getTraceQuestionDetail error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function createTraceQuestion(req, res) {
  try {
    const { errorTypeId, questionText, option1, option2, option3, option4 } = req.body;

    if (!errorTypeId || !questionText || !questionText.trim()) {
      return fail(res, "Thiếu thông tin câu hỏi", 400);
    }

    const data = await staffService.createTraceQuestion({
      errorTypeId,
      questionText: questionText.trim(),
      option1: option1 ? option1.trim() : "",
      option2: option2 ? option2.trim() : "",
      option3: option3 ? option3.trim() : "",
      option4: option4 ? option4.trim() : ""
    });

    return ok(res, data, "Tạo câu hỏi thành công", 201);
  } catch (error) {
    console.error("createTraceQuestion error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function updateTraceQuestion(req, res) {
  try {
    const { questionId } = req.params;
    const { errorTypeId, questionText, option1, option2, option3, option4 } = req.body;

    if (!questionId) {
      return fail(res, "Thiếu questionId", 400);
    }

    if (!errorTypeId || !questionText || !questionText.trim()) {
      return fail(res, "Thiếu thông tin cập nhật câu hỏi", 400);
    }

    const updated = await staffService.updateTraceQuestion(questionId, {
      errorTypeId,
      questionText: questionText.trim(),
      option1: option1 ? option1.trim() : "",
      option2: option2 ? option2.trim() : "",
      option3: option3 ? option3.trim() : "",
      option4: option4 ? option4.trim() : ""
    });

    if (!updated) {
      return fail(res, "Không tìm thấy câu hỏi", 404);
    }

    return ok(res, null, "Cập nhật câu hỏi thành công", 200);
  } catch (error) {
    console.error("updateTraceQuestion error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function deleteTraceQuestion(req, res) {
  try {
    const { questionId } = req.params;

    if (!questionId) {
      return fail(res, "Thiếu questionId", 400);
    }

    const deleted = await staffService.deleteTraceQuestion(questionId);

    if (!deleted) {
      return fail(res, "Không tìm thấy câu hỏi", 404);
    }

    return ok(res, null, "Xóa câu hỏi thành công", 200);
  } catch (error) {
    console.error("deleteTraceQuestion error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getQuestMonthlyMetrics(req, res) {
  try {
    const data = await staffService.getQuestMonthlyMetrics();
    return ok(res, data, "Lấy chỉ số quest theo tháng thành công", 200);
  } catch (error) {
    console.error("getQuestMonthlyMetrics error:", error);
    return fail(res, "Lỗi server", 500, error.message);
  }
}

async function getQuestRankingBoard(req, res) {
  try {
    const data = await staffService.getQuestRankingBoard();
    return ok(res, data, "Lấy BXH quest thành công", 200);
  } catch (error) {
    console.error("getQuestRankingBoard error:", error);
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
  getQuestReport,
  getQuestTrendReport,
  getAllTraceQuestions,
  getTraceQuestionDetail,
  createTraceQuestion,
  updateTraceQuestion,
  deleteTraceQuestion,
  getQuestMonthlyMetrics,
  getQuestRankingBoard
};
