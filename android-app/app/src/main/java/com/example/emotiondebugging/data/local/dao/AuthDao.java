package com.example.emotiondebugging.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.emotiondebugging.model.domain.User;

@Dao
public interface AuthDao {

    @Query(
            "SELECT " +
                    "u.user_id AS userId, " +
                    "u.name AS name, " +
                    "u.email AS email, " +
                    "u.password_hash AS passwordHash, " +
                    "u.is_locked AS locked, " +
                    "CASE " +
                    "   WHEN s.student_id IS NOT NULL THEN 'STUDENT' " +
                    "   WHEN a.admin_id IS NOT NULL THEN 'ADMIN' " +
                    "   WHEN st.staff_id IS NOT NULL THEN 'STAFF' " +
                    "   ELSE 'USER' " +
                    "END AS role, " +
                    "s.student_id AS studentId, " +
                    "s.student_code AS studentCode, " +
                    "a.admin_role AS adminRole, " +
                    "st.position AS staffPosition " +
                    "FROM USERS u " +
                    "LEFT JOIN STUDENTS s ON u.user_id = s.user_id " +
                    "LEFT JOIN ADMINS a ON u.user_id = a.user_id " +
                    "LEFT JOIN STAFF st ON u.user_id = st.user_id " +
                    "WHERE (u.email = :account OR s.student_code = :account) " +
                    "LIMIT 1"
    )
    User findUserForLogin(String account);
}