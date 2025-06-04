package com.example.physiotherapyapp.data

import java.util.Date
import java.util.UUID

/**
 * Egzersiz modelini temsil eder
 */
data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val isCompleted: Boolean = false
)

/**
 * Seans modelini temsil eder
 */
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val exercises: List<Exercise>,
    val startDate: Date = Date(),
    val endDate: Date? = null,
    val isCompleted: Boolean = false,
    val pointsEarned: Int = 0
)

/**
 * Kullanıcı modelini temsil eder (profil bilgileri için)
 */
data class User(
    val totalSessions: Int = 0,
    val totalPoints: Int = 0,
    val completedSessions: List<Session> = emptyList()
) 