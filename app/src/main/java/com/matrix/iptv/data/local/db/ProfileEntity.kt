package com.matrix.iptv.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val host: String,
    val username: String,
    val createdAt: Long,
    val lastUsed: Long
)
