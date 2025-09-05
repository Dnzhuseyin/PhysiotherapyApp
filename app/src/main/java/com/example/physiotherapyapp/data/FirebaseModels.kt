package com.example.physiotherapyapp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Firebase Firestore veri modelleri
 */

/**
 * Firestore User dokümanı
 */
data class FirestoreUser(
    @DocumentId val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val totalSessions: Int = 0,
    val totalPoints: Int = 0,
    val currentLevel: Int = 1,
    val badges: List<String> = emptyList(),
    val profileCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * Firestore UserProfile dokümanı
 */
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "", // UserCategory.name
    val ageGroup: String = "", // AgeGroup.name
    val activityLevel: String = "", // ActivityLevel.name
    val primaryComplaint: String = "",
    val goal: String = "",
    val limitations: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * Firestore Exercise dokümanı
 */
data class FirestoreExercise(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val difficulty: String = "BEGINNER", // BEGINNER, INTERMEDIATE, ADVANCED
    val duration: Int = 0, // dakika
    val instructions: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    val imageUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * Firestore SessionTemplate dokümanı
 */
data class FirestoreSessionTemplate(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val exercises: List<String> = emptyList(), // Exercise ID'leri
    val estimatedDuration: String = "",
    val isAIGenerated: Boolean = false,
    val aiConfidence: Float = 0f,
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * Firestore Session dokümanı (tamamlanan seanslar)
 */
data class FirestoreSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val templateId: String = "",
    val templateName: String = "",
    val exercises: List<FirestoreSessionExercise> = emptyList(),
    val totalExercises: Int = 0,
    val completedExercises: Int = 0,
    val pointsEarned: Int = 10,
    val status: String = "COMPLETED", // ACTIVE, COMPLETED, CANCELLED
    @ServerTimestamp val startedAt: Timestamp? = null,
    @ServerTimestamp val completedAt: Timestamp? = null
)

/**
 * Session içindeki egzersiz
 */
data class FirestoreSessionExercise(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val isCompleted: Boolean = false,
    @ServerTimestamp val completedAt: Timestamp? = null
)

/**
 * Firestore PainEntry dokümanı
 */
data class FirestorePainEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sessionId: String? = null,
    val painLevel: Int = 0, // 0-10 arası
    val bodyPart: String = "",
    val notes: String = "",
    val mood: String = "", // GOOD, NEUTRAL, BAD
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * Firestore Badge dokümanı
 */
data class FirestoreBadge(
    @DocumentId val id: String = "",
    val userId: String = "",
    val badgeType: String = "", // BadgeCategory.name
    val name: String = "",
    val description: String = "",
    val iconName: String = "",
    val requirement: Int = 0, // Gereken sayı (seans, gün, puan vb.)
    val currentProgress: Int = 0,
    val isUnlocked: Boolean = false,
    @ServerTimestamp val unlockedAt: Timestamp? = null
)

/**
 * Firestore ProgressReport dokümanı
 */
data class FirestoreProgressReport(
    @DocumentId val id: String = "",
    val userId: String = "",
    val reportType: String = "", // WEEKLY, MONTHLY, CUSTOM
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val totalSessions: Int = 0,
    val totalPoints: Int = 0,
    val averagePainLevel: Double = 0.0,
    val mostFrequentExercises: List<String> = emptyList(),
    val improvementScore: Double = 0.0,
    val reportData: Map<String, Any> = emptyMap(),
    @ServerTimestamp val generatedAt: Timestamp? = null
)

/**
 * Firestore Reminder dokümanı
 */
data class FirestoreReminder(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val time: String = "", // HH:mm formatında
    val daysOfWeek: List<Int> = emptyList(), // 1=Pazartesi, 7=Pazar
    val isEnabled: Boolean = true,
    val reminderType: String = "EXERCISE", // EXERCISE, PAIN_LOG, CUSTOM
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * Firestore AIRecommendation dokümanı
 */
data class FirestoreAIRecommendation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userProfileSnapshot: Map<String, Any> = emptyMap(),
    val sessionName: String = "",
    val description: String = "",
    val exercises: List<String> = emptyList(), // Exercise isimleri
    val estimatedDuration: String = "",
    val confidence: Float = 0.9f,
    val specialNotes: String = "",
    val improvements: List<String> = emptyList(),
    val isAccepted: Boolean = false,
    @ServerTimestamp val generatedAt: Timestamp? = null,
    @ServerTimestamp val acceptedAt: Timestamp? = null
)

/**
 * Local models ile Firebase models arasında dönüşüm fonksiyonları
 */

// User dönüşümleri
fun User.toFirestore(uid: String): FirestoreUser {
    return FirestoreUser(
        uid = uid,
        displayName = name,
        totalSessions = totalSessions,
        totalPoints = totalPoints,
        currentLevel = currentLevel,
        badges = badges.map { it.id },
        profileCompleted = true
    )
}

fun FirestoreUser.toLocal(): User {
    return User(
        name = displayName,
        totalSessions = totalSessions,
        totalPoints = totalPoints,
        currentLevel = currentLevel,
        // badges ve diğer complex objeler ayrı query'lerle doldurulacak
        badges = emptyList(),
        goals = PersonalGoals(),
        painEntries = emptyList(),
        completedSessions = emptyList()
    )
}

// UserProfile dönüşümleri  
fun userProfileToFirestore(profile: UserProfile, userId: String): FirestoreUserProfile {
    return FirestoreUserProfile(
        userId = userId,
        category = profile.category.name,
        ageGroup = profile.ageGroup.name,
        activityLevel = profile.activityLevel.name,
        primaryComplaint = profile.primaryComplaint,
        goal = profile.goal,
        limitations = profile.limitations
    )
}

fun FirestoreUserProfile.toLocal(): UserProfile {
    return UserProfile(
        category = UserCategory.valueOf(category),
        ageGroup = AgeGroup.valueOf(ageGroup),
        activityLevel = ActivityLevel.valueOf(activityLevel),
        primaryComplaint = primaryComplaint,
        goal = goal,
        limitations = limitations
    )
}

// SessionTemplate dönüşümleri
fun SessionTemplate.toFirestore(userId: String): FirestoreSessionTemplate {
    return FirestoreSessionTemplate(
        userId = userId,
        name = name,
        exercises = exercises.map { it.name }, // Exercise name'leri
        estimatedDuration = estimatedDuration,
        isAIGenerated = false
    )
}

// PainEntry dönüşümleri
fun painEntryToFirestore(painEntry: PainEntry, userId: String): FirestorePainEntry {
    return FirestorePainEntry(
        userId = userId,
        sessionId = painEntry.sessionId,
        painLevel = painEntry.painLevel,
        bodyPart = painEntry.bodyPart,
        notes = painEntry.notes
    )
}

fun FirestorePainEntry.toLocal(): PainEntry {
    return PainEntry(
        id = id,
        sessionId = sessionId ?: "",
        painLevel = painLevel,
        bodyPart = bodyPart,
        notes = notes,
        date = createdAt?.toDate() ?: Date()
    )
}
