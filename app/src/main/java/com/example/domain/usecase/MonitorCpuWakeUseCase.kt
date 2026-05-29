package com.example.domain.usecase

import com.example.domain.model.CpuWakeEvent
import com.example.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MonitorCpuWakeUseCase @Inject constructor(
    private val telemetryRepository: TelemetryRepository
) {
    operator fun invoke(startTime: Long): Flow<List<CpuWakeEvent>> {
        return telemetryRepository.getCpuWakeEvents(startTime)
    }
}
