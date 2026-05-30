package com.michael.frozendroid.domain.model

data class CpuWakeEvent(
    val id: Long = 0L,
    val packageName: String,
    val timestamp: Long,
    val wakeCount: Int,
    val durationMs: Long,
    val batteryDelta: Float
)
