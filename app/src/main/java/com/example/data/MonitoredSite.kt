package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitored_sites")
data class MonitoredSite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastChecked: Long? = null,
    val lastStatusCode: Int? = null,
    val lastLatencyMs: Long? = null,
    val lastStatus: String? = null, // "UP", "DEGRADED", "DOWN"
    val lastErrorMessage: String? = null,
    val totalChecks: Int = 0,
    val successfulChecks: Int = 0
) {
    val uptimePercentage: Float
        get() = if (totalChecks > 0) {
            (successfulChecks.toFloat() / totalChecks.toFloat()) * 100f
        } else {
            100f
        }
}
