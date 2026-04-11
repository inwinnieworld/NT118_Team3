package com.nt118.team3.emotedebugging.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ADMINS")
public class AdminEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "admin_id")
    public int adminId;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "admin_role")
    public String adminRole;
}