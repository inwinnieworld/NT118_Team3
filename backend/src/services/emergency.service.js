const db = require("../config/db");

async function getResources() {
  const [rows] = await db.execute(
    `SELECT resource_id, resource_name, phone, sms_phone, resource_type
     FROM EMERGENCY_RESOURCES
     WHERE is_active = TRUE
     ORDER BY resource_type, resource_name`
  );
  return rows;
}

async function getContactsByStudentId(studentId) {
  const [rows] = await db.execute(
    `SELECT contact_id, student_id, contact_name, phone, relationship
     FROM emergency_contacts
     WHERE student_id = ?
     ORDER BY contact_name`,
    [studentId]
  );
  return rows;
}

async function createContact({ studentId, contactName, phone, relationship }) {
  const [result] = await db.execute(
    `INSERT INTO emergency_contacts (student_id, contact_name, phone, relationship)
     VALUES (?, ?, ?, ?)`,
    [studentId, contactName, phone, relationship]
  );

  return {
    contact_id: result.insertId,
    student_id: studentId,
    contact_name: contactName,
    phone,
    relationship
  };
}

async function updateContact(contactId, { contactName, phone, relationship }) {
  await db.execute(
    `UPDATE emergency_contacts
     SET contact_name = ?, phone = ?, relationship = ?
     WHERE contact_id = ?`,
    [contactName, phone, relationship, contactId]
  );
}

async function deleteContact(contactId) {
  await db.execute(
    `DELETE FROM emergency_contacts WHERE contact_id = ?`,
    [contactId]
  );
}

module.exports = {
  getResources,
  getContactsByStudentId,
  createContact,
  updateContact,
  deleteContact
};