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
    val name: String = "Kullanıcı",
    val totalSessions: Int = 0,
    val totalPoints: Int = 0,
    val completedSessions: List<Session> = emptyList(),
    val badges: List<Badge> = emptyList(),
    val currentLevel: Int = 1,
    val goals: PersonalGoals = PersonalGoals(),
    val painEntries: List<PainEntry> = emptyList()
)

/**
 * Ağrı günlüğü girdisi
 */
data class PainEntry(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val date: Date = Date(),
    val painLevel: Int, // 0-10 arası
    val bodyPart: String = "",
    val notes: String = ""
)

/**
 * Rozet sistemi
 */
data class Badge(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val iconName: String,
    val unlockedDate: Date = Date(),
    val category: BadgeCategory
)

enum class BadgeCategory {
    SESSIONS, // Seans sayısı rozetleri
    CONSISTENCY, // Süreklilik rozetleri
    POINTS, // Puan rozetleri
    SPECIAL // Özel rozetler
}

/**
 * Kişisel hedefler
 */
data class PersonalGoals(
    val dailySessionTarget: Int = 1,
    val weeklySessionTarget: Int = 5,
    val dailyPointTarget: Int = 10,
    val weeklyPointTarget: Int = 50,
    val currentDailyStreak: Int = 0,
    val bestDailyStreak: Int = 0
)

/**
 * Notification/Hatırlatıcı
 */
data class ReminderNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val time: String, // HH:mm formatında
    val isEnabled: Boolean = true,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7) // 1=Pazartesi, 7=Pazar
)

/**
 * İlerleme raporu
 */
data class ProgressReport(
    val id: String = UUID.randomUUID().toString(),
    val startDate: Date,
    val endDate: Date,
    val totalSessions: Int,
    val totalPoints: Int,
    val avgPainLevel: Double,
    val mostFrequentExercises: List<String>,
    val weeklyProgress: List<WeeklyProgress>
)

/**
 * Haftalık ilerleme
 */
data class WeeklyProgress(
    val weekStartDate: Date,
    val sessionsCompleted: Int,
    val pointsEarned: Int,
    val avgPainLevel: Double
) 