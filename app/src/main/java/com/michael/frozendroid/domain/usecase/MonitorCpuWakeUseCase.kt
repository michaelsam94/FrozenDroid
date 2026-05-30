package com.michael.frozendroid.domain.usecase

import com.michael.frozendroid.domain.model.CpuWakeEvent
import com.michael.frozendroid.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MonitorCpuWakeUseCase @Inject constructor(
    private val telemetryRepository: TelemetryRepository
) {
    operator fun invoke(startTime: Long): Flow<List<CpuWakeEvent>> {
        return telemetryRepository.getCpuWakeEvents(startTime)
    }
}
