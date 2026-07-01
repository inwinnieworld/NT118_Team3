const db = require("../config/db");

/* =========================
   QUEST
========================= */
async function assertLeafProblem(problemId) {
  const [[problem]] = await db.execute(
    `SELECT id FROM PROBLEMS
     WHERE id = ? AND tree_level = 3 AND is_leaf_node = TRUE`,
    [problemId]
  );
  if (!problem) {
    const error = new Error("problemId must reference a level-3 leaf problem");
    error.status = 400;
    throw error;
  }
}

async function createQuest({ problemId, questTitle, questDescription, questLevel = 3, basePriority = 10, createdByStaffId = null }) {
  await assertLeafProblem(problemId);
  const [result] = await db.execute(
    `INSERT INTO QUESTS
       (created_by_staff_id, problem_id, quest_title, quest_description,
        quest_level, base_priority, is_active)
     VALUES (?, ?, ?, ?, ?, ?, TRUE)`,
    [createdByStaffId, problemId, questTitle, questDescription, questLevel, basePriority]
  );

  return {
    quest_id: result.insertId,
    problem_id: problemId,
    quest_title: questTitle,
    quest_description: questDescription,
    quest_level: questLevel,
    base_priority: basePriority
  };
}

async function updateQuest(questId, { problemId, questTitle, questDescription, questLevel, basePriority }) {
  await assertLeafProblem(problemId);
  const [result] = await db.execute(
     `UPDATE QUESTS
     SET problem_id = ?, error_type_id = NULL, quest_title = ?,
         quest_description = ?, quest_level = COALESCE(?, quest_level),
         base_priority = COALESCE(?, base_priority)
     WHERE quest_id = ?`,
    [problemId, questTitle, questDescription, questLevel, basePriority, questId]
  );

  return result.affectedRows > 0;
}

async function getAllQuests() {
  const [rows] = await db.execute(
    `SELECT 
       q.quest_id,
       q.problem_id,
       p.title AS problem_title,
       CONCAT_WS(' > ', root_problem.title, parent_problem.title, p.title) AS problem_path,
       q.quest_title,
       q.quest_description,
       q.quest_level,
       q.base_priority,
       q.is_active,
       q.created_at,
       GROUP_CONCAT(DISTINCT opt.tag_core_name SEPARATOR ', ') AS tags
     FROM QUESTS q
     LEFT JOIN PROBLEMS p ON p.id = q.problem_id
     LEFT JOIN PROBLEMS parent_problem ON parent_problem.id = p.parent_id
     LEFT JOIN PROBLEMS root_problem ON root_problem.id = parent_problem.parent_id
     LEFT JOIN QUEST_TAG_MAPPING qtm ON q.quest_id = qtm.quest_id
     LEFT JOIN TRACE_OPTIONS opt ON qtm.option_id = opt.option_id
     WHERE q.is_active = TRUE
     GROUP BY q.quest_id, q.problem_id, p.title, root_problem.title, parent_problem.title,
              q.quest_title, q.quest_description, q.quest_level, q.base_priority,
              q.is_active, q.created_at
     ORDER BY q.quest_id DESC`
  );

  return rows;
}

async function deleteQuest(questId) {
  const [[used]] = await db.execute(
    `SELECT COUNT(*) AS total
     FROM USER_QUESTS
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

/* =========================
   ASSIGNMENT
========================= */
async function assignQuestToStudent({ studentId, questId, errorLogId, status = "pending" }) {
  const [result] = await db.execute(
    `INSERT INTO USER_QUESTS (student_id, quest_id, log_id, status)
     VALUES (?, ?, ?, ?)`,
    [studentId, questId, errorLogId, status]
  );

  return {
    user_quest_id: result.insertId,
    student_id: studentId,
    quest_id: questId,
    log_id: errorLogId,
    status
  };
}

async function getQuestAssignments() {
  const [rows] = await db.execute(
    `SELECT uq.user_quest_id, uq.student_id, s.student_code,
            uq.quest_id, q.quest_title,
            uq.log_id, el.severity_level,
            uq.status, uq.assigned_at, uq.completed_at
     FROM USER_QUESTS uq
     JOIN STUDENTS s ON uq.student_id = s.student_id
     JOIN QUESTS q ON uq.quest_id = q.quest_id
     JOIN ERROR_LOGS el ON uq.log_id = el.log_id
     ORDER BY uq.assigned_at DESC`
  );

  return rows;
}

/* =========================
   REPORT
========================= */
async function getSummaryReport() {
  const [[errorCount]] = await db.execute(
    `SELECT COUNT(*) AS total_error_logs FROM ERROR_LOGS`
  );

  const [[questCount]] = await db.execute(
    `SELECT COUNT(*) AS total_quests FROM QUESTS WHERE is_active = TRUE`
  );

  const [[assignmentCount]] = await db.execute(
    `SELECT COUNT(*) AS total_assignments FROM USER_QUESTS`
  );

  const [[completedCount]] = await db.execute(
    `SELECT COUNT(*) AS completed_assignments
     FROM USER_QUESTS
     WHERE status = 'completed'`
  );

  const [[avgEffectiveness]] = await db.execute(
    `SELECT ROUND(AVG(effectiveness_rating), 2) AS avg_effectiveness
     FROM USER_QUESTS
     WHERE effectiveness_rating IS NOT NULL`
  );

  return {
    total_error_logs: errorCount.total_error_logs,
    total_quests: questCount.total_quests,
    total_assignments: assignmentCount.total_assignments,
    completed_assignments: completedCount.completed_assignments,
    avg_effectiveness: avgEffectiveness.avg_effectiveness || 0
  };
}

async function getErrorReport() {
  const [rows] = await db.execute(
    `SELECT et.error_type_id, et.error_name, COUNT(el.log_id) AS total_logs
     FROM ERROR_TYPES et
     LEFT JOIN ERROR_LOGS el ON et.error_type_id = el.error_type_id
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
     LEFT JOIN USER_QUESTS uq ON q.quest_id = uq.quest_id
     WHERE q.is_active = TRUE
     GROUP BY q.quest_id, q.quest_title
     ORDER BY total_assigned DESC, q.quest_title`
  );

  return rows;
}

async function getQuestTrendReport() {
  const [assignedRows] = await db.execute(`
    SELECT 
      DATE(assigned_at) AS chart_date,
      COUNT(*) AS total_assigned
    FROM USER_QUESTS
    GROUP BY DATE(assigned_at)
    ORDER BY DATE(assigned_at) ASC
  `);

  const [completedRows] = await db.execute(`
    SELECT 
      DATE(completed_at) AS chart_date,
      COUNT(*) AS total_completed
    FROM USER_QUESTS
    WHERE completed_at IS NOT NULL
    GROUP BY DATE(completed_at)
    ORDER BY DATE(completed_at) ASC
  `);

  return {
    assigned: assignedRows,
    completed: completedRows
  };
}

/* =========================
   TRACE QUESTIONS
========================= */
async function getAllTraceQuestions() {
  const [rows] = await db.execute(`
    SELECT 
      tq.question_id,
      tq.error_type_id,
      et.error_name,
      tq.question_text,
      tq.is_active
    FROM TRACE_QUESTIONS tq
    JOIN ERROR_TYPES et ON tq.error_type_id = et.error_type_id
    WHERE tq.is_active = TRUE
    ORDER BY et.error_name ASC, tq.question_id ASC
  `);

  return rows;
}

async function getTraceQuestionDetail(questionId) {
  const [rows] = await db.execute(`
    SELECT 
      tq.question_id,
      tq.error_type_id,
      et.error_name,
      tq.question_text,
      tq.is_active
    FROM TRACE_QUESTIONS tq
    JOIN ERROR_TYPES et ON tq.error_type_id = et.error_type_id
    WHERE tq.question_id = ?
    LIMIT 1
  `, [questionId]);

  return rows.length > 0 ? rows[0] : null;
}

async function createTraceQuestion({
  errorTypeId,
  questionText
}) {
  const [result] = await db.execute(
    `INSERT INTO TRACE_QUESTIONS
      (error_type_id, question_text, is_active)
     VALUES (?, ?, TRUE)`,
    [errorTypeId, questionText]
  );

  return {
    question_id: result.insertId,
    error_type_id: errorTypeId,
    question_text: questionText,
    is_active: true
  };
}

async function updateTraceQuestion(
  questionId,
  { errorTypeId, questionText }
) {
  const [result] = await db.execute(
    `UPDATE TRACE_QUESTIONS
     SET error_type_id = ?,
         question_text = ?
     WHERE question_id = ?`,
    [errorTypeId, questionText, questionId]
  );

  return result.affectedRows > 0;
}

async function deleteTraceQuestion(questionId) {
  const [result] = await db.execute(
    `DELETE FROM TRACE_QUESTIONS
     WHERE question_id = ?`,
    [questionId]
  );

  return result.affectedRows > 0;
}

async function getQuestMonthlyMetrics() {
  const [rows] = await db.execute(`
    SELECT
      DATE_FORMAT(el.created_at, '%Y-%m') AS chart_month,
      ROUND(AVG(el.severity_level), 2) AS avg_severity,
      ROUND(SUM(CASE WHEN el.severity_level >= 4 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS severity_rate,
      COUNT(el.log_id) AS total_errors,
      ROUND(
        SUM(CASE WHEN uq.status = 'completed' THEN 1 ELSE 0 END) * 100.0 /
        NULLIF(COUNT(uq.user_quest_id), 0), 2
      ) AS acceptance_rate
    FROM ERROR_LOGS el
    LEFT JOIN USER_QUESTS uq ON uq.log_id = el.log_id
    GROUP BY DATE_FORMAT(el.created_at, '%Y-%m')
    ORDER BY DATE_FORMAT(el.created_at, '%Y-%m') ASC
  `);

  return rows;
}

async function getQuestRankingBoard() {
  const [rows] = await db.execute(`
    SELECT
      q.quest_id,
      q.quest_title,
      COUNT(uq.user_quest_id) AS total_assigned,
      SUM(CASE WHEN uq.status = 'completed' THEN 1 ELSE 0 END) AS total_completed
    FROM QUESTS q
    LEFT JOIN USER_QUESTS uq ON uq.quest_id = q.quest_id
    WHERE q.is_active = TRUE
    GROUP BY q.quest_id, q.quest_title
    ORDER BY total_assigned DESC, total_completed DESC, q.quest_id ASC
  `);

  return rows;
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
  getQuestMonthlyMetrics,
  getQuestRankingBoard
};
