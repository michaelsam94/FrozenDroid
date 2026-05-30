package com.michael.frozendroid.domain.repository

import com.michael.frozendroid.domain.model.CpuWakeEvent
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    fun getCpuWakeEvents(startTime: Long): Flow<List<CpuWakeEvent>>
    suspend fun insertCpuWakeEvents(events: List<CpuWakeEvent>)
    suspend fun pruneOldEvents(beforeTimestamp: Long)
    suspend fun deleteFabricatedWakeEvents()
}
