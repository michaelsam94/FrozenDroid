package com.example.data.repository

import com.example.data.local.daos.TelemetryDao
import com.example.data.local.entities.CpuWakeEventEntity
import com.example.domain.model.CpuWakeEvent
import com.example.domain.repository.TelemetryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryRepositoryImpl @Inject constructor(
    private val telemetryDao: TelemetryDao
) : TelemetryRepository {

    override fun getCpuWakeEvents(startTime: Long): Flow<List<CpuWakeEvent>> {
        return telemetryDao.getCpuWakeEventsFlow(startTime).map { entities ->
            entities.map { entity ->
                CpuWakeEvent(
                    id = entity.id,
                    packageName = entity.packageName,
                    timestamp = entity.timestamp,
                    wakeCount = entity.wakeCount,
                    durationMs = entity.durationMs,
                    batteryDelta = eventEntityToUsage(entity)
                )
            }
        }.flowOn(Dispatchers.Default)
    }

    private fun eventEntityToUsage(entity: CpuWakeEventEntity): Float {
        return entity.batteryDelta
    }

    override suspend fun insertCpuWakeEvents(events: List<CpuWakeEvent>) = withContext(Dispatchers.IO) {
        val entities = events.map { event ->
            CpuWakeEventEntity(
                packageName = event.packageName,
                timestamp = event.timestamp,
                wakeCount = event.wakeCount,
                durationMs = event.durationMs,
                batteryDelta = event.batteryDelta
            )
        }
        telemetryDao.insertCpuWakeEvents(entities)
    }

    override suspend fun pruneOldEvents(beforeTimestamp: Long) = withContext(Dispatchers.IO) {
        telemetryDao.pruneOldEvents(beforeTimestamp)
    }
}
