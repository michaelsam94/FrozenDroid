package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cpu_wake_events")
data class CpuWakeEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val timestamp: Long,
    val wakeCount: Int,
    val durationMs: Long,
    val batteryDelta: Float
)
