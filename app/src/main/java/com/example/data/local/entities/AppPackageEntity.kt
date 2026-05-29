package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_packages")
data class AppPackageEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val icon: String?,
    val isFrozen: Boolean,
    val safetyLevel: String, // String representation of SafetyLevel enum
    val carrier: String
)
