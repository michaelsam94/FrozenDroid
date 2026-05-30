package com.michael.frozendroid.domain.model

data class FreezeProfile(
    val id: String,
    val name: String,
    val icon: String,
    val frozenPackages: List<String>,
    val scheduleTimes: List<String>,
    val isActive: Boolean
)
