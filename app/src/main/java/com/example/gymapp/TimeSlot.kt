package com.example.gymapp

data class TimeSlot(
    val startTime: String,
    val endTime: String,
    var isSelected: Boolean = false
)