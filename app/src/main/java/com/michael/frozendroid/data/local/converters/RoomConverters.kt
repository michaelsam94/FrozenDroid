package com.michael.frozendroid.data.local.converters

import androidx.room.TypeConverter

class RoomConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",")
    }
}
