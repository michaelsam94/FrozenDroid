package com.michael.frozendroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_overrides")
data class UserOverrideEntity(
    @PrimaryKey val packageName: String,
    val customSafetyLevel: String // Resolved as SafetyLevel
)
