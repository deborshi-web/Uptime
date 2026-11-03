package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    @Query("SELECT * FROM monitored_sites ORDER BY id ASC")
    fun getAllSites(): Flow<List<MonitoredSite>>

    @Query("SELECT * FROM monitored_sites WHERE id = :id")
    fun getSiteById(id: Long): Flow<MonitoredSite?>

    @Query("SELECT * FROM check_logs WHERE siteId = :siteId ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsForSite(siteId: Long, limit: Int = 30): Flow<List<CheckLog>>

    @Query("SELECT * FROM check_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getAllRecentLogs(limit: Int = 50): Flow<List<CheckLog>>

    @Query("SELECT * FROM check_logs WHERE status != 'UP' ORDER BY timestamp DESC LIMIT :limit")
    fun getIncidentLogs(limit: Int = 50): Flow<List<CheckLog>>

    @Query("SELECT COUNT(*) FROM monitored_sites")
    suspend fun getSiteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: MonitoredSite): Long

    @Update
    suspend fun updateSite(site: MonitoredSite)

    @Delete
    suspend fun deleteSite(site: MonitoredSite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CheckLog): Long

    @Query("DELETE FROM check_logs WHERE siteId = :siteId")
    suspend fun deleteLogsForSite(siteId: Long)
}
