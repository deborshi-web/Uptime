package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_logs")
data class CheckLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteId: Long,
    val siteName: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val statusCode: Int = 0,
    val latencyMs: Long = 0L,
    val dnsTimeMs: Long = 0L,
    val connectTimeMs: Long = 0L,
    val status: String, // "UP", "DEGRADED", "DOWN"
    val protocol: String = "HTTP",
    val ipAddress: String = "Unknown",
    val tlsVersion: String? = null,
    val errorMessage: String? = null
)
