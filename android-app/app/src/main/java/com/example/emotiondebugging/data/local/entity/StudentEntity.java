package com.nt118.team3.emotedebugging.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "STUDENTS")
public class StudentEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "student_id")
    public int studentId;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "student_code")
    public String studentCode;

    public String major;
    public String faculty;

    @ColumnInfo(name = "year_of_study")
    public int yearOfStudy;
}