package com.nt118.team3.emotedebugging.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "STAFF")
public class StaffEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "staff_id")
    public int staffId;

    @ColumnInfo(name = "user_id")
    public int userId;

    public String position;
    public String department;

    @ColumnInfo(name = "hire_date")
    public String hireDate;
}