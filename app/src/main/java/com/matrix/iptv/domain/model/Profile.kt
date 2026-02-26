package com.matrix.iptv.domain.model

data class Profile(
    val id: String,
    val name: String,
    val host: String,
    val username: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = 0L
)
