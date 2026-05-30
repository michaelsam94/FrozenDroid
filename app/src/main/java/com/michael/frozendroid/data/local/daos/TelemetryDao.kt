package com.michael.frozendroid.data.local.daos

import androidx.room.*
import com.michael.frozendroid.data.local.entities.CpuWakeEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM cpu_wake_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getCpuWakeEventsFlow(startTime: Long): Flow<List<CpuWakeEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCpuWakeEvents(events: List<CpuWakeEventEntity>)

    @Query("DELETE FROM cpu_wake_events WHERE timestamp < :beforeTimestamp")
    suspend fun pruneOldEvents(beforeTimestamp: Long)

    @Query("DELETE FROM cpu_wake_events WHERE batteryDelta > 0")
    suspend fun deleteFabricatedWakeEvents()
}
