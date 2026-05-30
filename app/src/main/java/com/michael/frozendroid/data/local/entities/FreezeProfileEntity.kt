package com.michael.frozendroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "freeze_profiles")
data class FreezeProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val frozenPackages: List<String>, // Saved via TypeConverter integration
    val scheduleTimes: List<String>,   // Saved via TypeConverter integration
    val isActive: Boolean
)
