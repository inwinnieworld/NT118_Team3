const db = require("../config/db");

async function createQuest({ errorTypeId, questTitle, questDescription }) {
  const [result] = await db.execute(
    `INSERT INTO QUESTS (error_type_id, quest_title, quest_description)
     VALUES (?, ?, ?)`,
    [errorTypeId, questTitle, questDescription]
  );

  return {
    quest_id: result.insertId,
    error_type_id: errorTypeId,
    quest_title: questTitle,
    quest_description: questDescription
  };
}

async function updateQuest(questId, { errorTypeId, questTitle, questDescription }) {
  await db.execute(
    `UPDATE QUESTS
     SET error_type_id = ?, quest_title = ?, quest_description = ?
     WHERE quest_id = ?`,
    [errorTypeId, questTitle, questDescription, questId]
  );
}

async function getAllQuests() {
  const [rows] = await db.execute(
    `SELECT q.quest_id, q.error_type_id, e.error_name, q.quest_title, q.quest_description
     FROM QUESTS q
     LEFT JOIN ERRORTYPES e ON q.error_type_id = e.error_type_id
     ORDER BY q.quest_id DESC`
  );
  return rows;
}

async function assignQuestToStudent({ studentId, questId, errorLogId, status = "pending" }) {
  const [result] = await db.execute(
    `INSERT INTO USERQUESTS (student_id, quest_id, error_log_id, status)
     VALUES (?, ?, ?, ?)`,
    [studentId, questId, errorLogId, status]
  );

  return {
    user_quest_id: result.insertId,
    student_id: studentId,
    quest_id: questId,
    error_log_id: errorLogId,
    status
  };
}

async function getQuestAssignments() {
  const [rows] = await db.execute(
    `SELECT uq.user_quest_id, uq.student_id, s.student_code,
            uq.quest_id, q.quest_title,
            uq.error_log_id, et.error_name,
            uq.status, uq.assigned_at, uq.completed_at
     FROM USERQUESTS uq
     JOIN STUDENTS s ON uq.student_id = s.student_id
     JOIN QUESTS q ON uq.quest_id = q.quest_id
     JOIN ERRORLOGS el ON uq.error_log_id = el.error_log_id
     JOIN ERRORTYPES et ON el.error_type_id = et.error_type_id
     ORDER BY uq.assigned_at DESC`
  );
  return rows;
}

async function getSummaryReport() {
  const [[errorCount]] = await db.execute(
    `SELECT COUNT(*) AS total_error_logs FROM ERRORLOGS`
  );

  const [[questCount]] = await db.execute(
    `SELECT COUNT(*) AS total_quests FROM QUESTS`
  );

  const [[assignmentCount]] = await db.execute(
    `SELECT COUNT(*) AS total_assignments FROM USERQUESTS`
  );

  const [[completedCount]] = await db.execute(
    `SELECT COUNT(*) AS completed_assignments
     FROM USERQUESTS
     WHERE status = 'completed'`
  );

  const [[avgFeedback]] = await db.execute(
    `SELECT ROUND(AVG(rating), 2) AS avg_feedback
     FROM FEEDBACK`
  );

  return {
    total_error_logs: errorCount.total_error_logs,
    total_quests: questCount.total_quests,
    total_assignments: assignmentCount.total_assignments,
    completed_assignments: completedCount.completed_assignments,
    avg_feedback: avgFeedback.avg_feedback || 0
  };
}

async function getErrorReport() {
  const [rows] = await db.execute(
    `SELECT et.error_type_id, et.error_name, COUNT(el.error_log_id) AS total_logs
     FROM ERRORTYPES et
     LEFT JOIN ERRORLOGS el ON et.error_type_id = el.error_type_id
     GROUP BY et.error_type_id, et.error_name
     ORDER BY total_logs DESC, et.error_name`
  );
  return rows;
}

async function getQuestReport() {
  const [rows] = await db.execute(
    `SELECT q.quest_id, q.quest_title,
            COUNT(uq.user_quest_id) AS total_assigned,
            SUM(CASE WHEN uq.status = 'completed' THEN 1 ELSE 0 END) AS total_completed
     FROM QUESTS q
     LEFT JOIN USERQUESTS uq ON q.quest_id = uq.quest_id
     GROUP BY q.quest_id, q.quest_title
     ORDER BY total_assigned DESC, q.quest_title`
  );
  return rows;
}

async function deleteQuest(questId) {
  const [[used]] = await db.execute(
    `SELECT COUNT(*) AS total
     FROM USERQUESTS
     WHERE quest_id = ?`,
    [questId]
  );

  if (used.total > 0) {
    return {
      success: false,
      status: 400,
      message: "Quest đã được gán cho sinh viên, không thể xóa"
    };
  }

  const [result] = await db.execute(
    `DELETE FROM QUESTS
     WHERE quest_id = ?`,
    [questId]
  );

  if (result.affectedRows === 0) {
    return {
      success: false,
      status: 404,
      message: "Không tìm thấy quest"
    };
  }

  return {
    success: true,
    status: 200,
    message: "Xóa quest thành công"
  };
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