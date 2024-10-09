package com.example.gymapp

data class TimeSlot(
    val startTime: String,
    val endTime: String,
    val availableSpots: Int = 20,
    var isSelected: Boolean = false
)