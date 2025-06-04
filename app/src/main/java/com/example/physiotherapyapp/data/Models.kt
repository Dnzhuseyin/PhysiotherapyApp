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
 * Seans şablonu modelini temsil eder (kayıtlı seans planları)
 */
data class SessionTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<Exercise>,
    val createdDate: Date = Date(),
    val estimatedDuration: String = "${exercises.size * 5} dk" // Her egzersiz için 5 dk tahmin
)

/**
 * Aktif/Tamamlanmış seans modelini temsil eder
 */
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val templateName: String,
    val exercises: List<Exercise>,
    val startDate: Date = Date(),
    val endDate: Date? = null,
    val isCompleted: Boolean = false,
    val pointsEarned: Int = 0,
    val currentExerciseIndex: Int = 0
)

/**
 * Kullanıcı modelini temsil eder (profil bilgileri için)
 */
data class User(
    val totalSessions: Int = 0,
    val totalPoints: Int = 0,
    val completedSessions: List<Session> = emptyList()
) 