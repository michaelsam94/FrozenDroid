package com.michael.frozendroid.domain.repository

import com.michael.frozendroid.domain.model.CpuWakeEvent

interface CpuWakeTelemetryCollector {
    suspend fun collectWakeEvents(): List<CpuWakeEvent>
}
