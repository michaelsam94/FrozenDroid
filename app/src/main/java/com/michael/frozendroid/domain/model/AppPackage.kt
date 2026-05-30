package com.michael.frozendroid.domain.model

data class AppPackage(
    val packageName: String,
    val label: String,
    val icon: String?,
    val isFrozen: Boolean,
    val safetyLevel: SafetyLevel,
    val carrier: String = "Universal",
    val description: String = ""
)
