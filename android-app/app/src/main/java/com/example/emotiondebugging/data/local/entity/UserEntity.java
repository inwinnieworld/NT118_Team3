package com.nt118.team3.emotedebugging.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "USERS")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    public String name;

    @NonNull
    public String email;

    @NonNull
    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    public String phone;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "is_locked")
    public boolean isLocked;
}