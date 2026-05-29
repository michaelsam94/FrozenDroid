package com.example.domain.repository

import com.example.domain.model.CpuWakeEvent
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    fun getCpuWakeEvents(startTime: Long): Flow<List<CpuWakeEvent>>
    suspend fun insertCpuWakeEvents(events: List<CpuWakeEvent>)
    suspend fun pruneOldEvents(beforeTimestamp: Long)
}
