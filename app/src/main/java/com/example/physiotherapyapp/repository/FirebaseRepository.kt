package com.example.physiotherapyapp.repository

import com.example.physiotherapyapp.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore Repository
 * Tüm Firebase işlemlerini yönetir
 */
class FirebaseRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    /**
     * Kullanıcı koleksiyonları
     */
    private fun usersCollection() = firestore.collection("users")
    private fun userProfilesCollection() = firestore.collection("userProfiles")
    private fun sessionTemplatesCollection() = firestore.collection("sessionTemplates")
    private fun sessionsCollection() = firestore.collection("sessions")
    private fun painEntriesCollection() = firestore.collection("painEntries")
    private fun badgesCollection() = firestore.collection("badges")
    private fun remindersCollection() = firestore.collection("reminders")
    private fun progressReportsCollection() = firestore.collection("progressReports")
    private fun aiRecommendationsCollection() = firestore.collection("aiRecommendations")
    
    // USER OPERATIONS
    
    /**
     * Kullanıcı bilgilerini getir
     */
    suspend fun getUser(): FirestoreUser? {
        return try {
            val userId = currentUserId ?: return null
            usersCollection()
                .document(userId)
                .get()
                .await()
                .toObject(FirestoreUser::class.java)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "User getirme hatası", e)
            null
        }
    }
    
    /**
     * Kullanıcı bilgilerini güncelle
     */
    suspend fun updateUser(user: User): Boolean {
        return try {
            val userId = currentUserId ?: return false
            val firestoreUser = user.toFirestore(userId)
            usersCollection()
                .document(userId)
                .set(firestoreUser)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "User güncelleme hatası", e)
            false
        }
    }
    
    // USER PROFILE OPERATIONS
    
    /**
     * Kullanıcı profilini kaydet
     */
    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        return try {
            val userId = currentUserId ?: return false
            val firestoreProfile = userProfileToFirestore(profile, userId)
            userProfilesCollection()
                .document(userId)
                .set(firestoreProfile)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Profil kaydetme hatası", e)
            false
        }
    }
    
    /**
     * Kullanıcı profilini getir
     */
    suspend fun getUserProfile(): UserProfile? {
        return try {
            val userId = currentUserId ?: return null
            userProfilesCollection()
                .document(userId)
                .get()
                .await()
                .toObject(FirestoreUserProfile::class.java)
                ?.toLocal()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Profil getirme hatası", e)
            null
        }
    }
    
    // SESSION TEMPLATE OPERATIONS
    
    /**
     * Session template kaydet
     */
    suspend fun saveSessionTemplate(template: SessionTemplate): Boolean {
        return try {
            val userId = currentUserId ?: return false
            val firestoreTemplate = template.toFirestore(userId)
            sessionTemplatesCollection()
                .add(firestoreTemplate)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Template kaydetme hatası", e)
            false
        }
    }
    
    /**
     * Kullanıcının session template'lerini getir
     */
    suspend fun getUserSessionTemplates(): List<SessionTemplate> {
        return try {
            val userId = currentUserId ?: return emptyList()
            sessionTemplatesCollection()
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(FirestoreSessionTemplate::class.java)
                .map { firestoreTemplate ->
                    // Firestore'dan local model'e dönüştür
                    SessionTemplate(
                        id = firestoreTemplate.id,
                        name = firestoreTemplate.name,
                        exercises = firestoreTemplate.exercises.map { exerciseName ->
                            Exercise(name = exerciseName, description = "")
                        },
                        estimatedDuration = firestoreTemplate.estimatedDuration,
                        createdDate = firestoreTemplate.createdAt?.toDate() ?: java.util.Date()
                    )
                }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Template'ler getirme hatası", e)
            emptyList()
        }
    }
    
    /**
     * Session template sil
     */
    suspend fun deleteSessionTemplate(templateId: String): Boolean {
        return try {
            sessionTemplatesCollection()
                .document(templateId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Template silme hatası", e)
            false
        }
    }
    
    // SESSION OPERATIONS
    
    /**
     * Tamamlanan seans kaydet
     */
    suspend fun saveCompletedSession(session: Session): Boolean {
        return try {
            val userId = currentUserId ?: return false
            
            val firestoreSession = FirestoreSession(
                userId = userId,
                templateId = session.templateId,
                templateName = session.templateName,
                exercises = session.exercises.map { exercise ->
                    FirestoreSessionExercise(
                        exerciseId = "", // Exercise ID yoksa boş
                        exerciseName = exercise.name,
                        isCompleted = exercise.isCompleted
                    )
                },
                totalExercises = session.exercises.size,
                completedExercises = session.exercises.count { it.isCompleted },
                pointsEarned = 10,
                status = "COMPLETED"
            )
            
            sessionsCollection()
                .add(firestoreSession)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Seans kaydetme hatası", e)
            false
        }
    }
    
    /**
     * Kullanıcının tamamlanan seanslarını getir
     */
    suspend fun getUserCompletedSessions(): List<Session> {
        return try {
            val userId = currentUserId ?: return emptyList()
            sessionsCollection()
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "COMPLETED")
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(FirestoreSession::class.java)
                .map { firestoreSession ->
                    Session(
                        id = firestoreSession.id,
                        templateId = firestoreSession.templateId,
                        templateName = firestoreSession.templateName,
                        exercises = firestoreSession.exercises.map { exercise ->
                            Exercise(
                                name = exercise.exerciseName,
                                description = "",
                                isCompleted = exercise.isCompleted
                            )
                        },
                        currentExerciseIndex = firestoreSession.exercises.size,
                        startDate = firestoreSession.startedAt?.toDate() ?: java.util.Date(),
                        endDate = firestoreSession.completedAt?.toDate()
                    )
                }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Seanslar getirme hatası", e)
            emptyList()
        }
    }
    
    // PAIN ENTRY OPERATIONS
    
    /**
     * Ağrı kaydı kaydet
     */
    suspend fun savePainEntry(painEntry: PainEntry): Boolean {
        return try {
            val userId = currentUserId ?: return false
            val firestorePainEntry = painEntryToFirestore(painEntry, userId)
            painEntriesCollection()
                .add(firestorePainEntry)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Ağrı kaydı kaydetme hatası", e)
            false
        }
    }
    
    /**
     * Kullanıcının ağrı kayıtlarını getir
     */
    suspend fun getUserPainEntries(): List<PainEntry> {
        return try {
            val userId = currentUserId ?: return emptyList()
            painEntriesCollection()
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(FirestorePainEntry::class.java)
                .map { it.toLocal() }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Ağrı kayıtları getirme hatası", e)
            emptyList()
        }
    }
    
    /**
     * Ağrı kaydını güncelle
     */
    suspend fun updatePainEntry(painEntry: PainEntry): Boolean {
        return try {
            val userId = currentUserId ?: return false
            val firestorePainEntry = painEntryToFirestore(painEntry, userId)
            painEntriesCollection()
                .document(painEntry.id)
                .set(firestorePainEntry)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Ağrı kaydı güncelleme hatası", e)
            false
        }
    }
    
    /**
     * Ağrı kaydını sil
     */
    suspend fun deletePainEntry(painEntryId: String): Boolean {
        return try {
            painEntriesCollection()
                .document(painEntryId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Ağrı kaydı silme hatası", e)
            false
        }
    }
    
    // BADGE OPERATIONS
    
    /**
     * Kullanıcı rozetlerini getir
     */
    suspend fun getUserBadges(): List<Badge> {
        return try {
            val userId = currentUserId ?: return emptyList()
            badgesCollection()
                .whereEqualTo("userId", userId)
                .whereEqualTo("isUnlocked", true)
                .get()
                .await()
                .toObjects(FirestoreBadge::class.java)
                .map { firestoreBadge ->
                    Badge(
                        id = firestoreBadge.id,
                        name = firestoreBadge.name,
                        description = firestoreBadge.description,
                        iconName = firestoreBadge.iconName,
                        category = BadgeCategory.valueOf(firestoreBadge.badgeType),
                        unlockedDate = firestoreBadge.unlockedAt?.toDate() ?: java.util.Date()
                    )
                }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Rozetler getirme hatası", e)
            emptyList()
        }
    }
    
    /**
     * Rozet kaydet
     */
    suspend fun saveBadge(badge: Badge): Boolean {
        return try {
            val userId = currentUserId ?: return false
            val firestoreBadge = FirestoreBadge(
                userId = userId,
                badgeType = badge.category.name,
                name = badge.name,
                description = badge.description,
                iconName = badge.iconName,
                isUnlocked = true
            )
            badgesCollection()
                .add(firestoreBadge)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Rozet kaydetme hatası", e)
            false
        }
    }
    
    // REMINDER OPERATIONS
    
    /**
     * Hatırlatıcıları getir
     */
    suspend fun getUserReminders(): List<ReminderNotification> {
        return try {
            val userId = currentUserId ?: return emptyList()
            remindersCollection()
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects(FirestoreReminder::class.java)
                .map { firestoreReminder ->
                    ReminderNotification(
                        id = firestoreReminder.id,
                        title = firestoreReminder.title,
                        message = firestoreReminder.message,
                        time = firestoreReminder.time,
                        daysOfWeek = firestoreReminder.daysOfWeek,
                        isEnabled = firestoreReminder.isEnabled
                    )
                }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Hatırlatıcılar getirme hatası", e)
            emptyList()
        }
    }
    
    /**
     * AI önerisi kaydet
     */
    suspend fun saveAIRecommendation(recommendation: AISessionRecommendation): Boolean {
        return try {
            val userId = currentUserId ?: return false
            val firestoreRecommendation = FirestoreAIRecommendation(
                userId = userId,
                sessionName = recommendation.sessionName,
                description = recommendation.description,
                exercises = recommendation.exercises,
                estimatedDuration = recommendation.estimatedDuration,
                confidence = recommendation.confidence,
                specialNotes = recommendation.specialNotes,
                improvements = recommendation.improvements
            )
            aiRecommendationsCollection()
                .add(firestoreRecommendation)
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "AI önerisi kaydetme hatası", e)
            false
        }
    }
    
    /**
     * Real-time data flow'lar
     */
    
    /**
     * Kullanıcı verilerini real-time dinle
     */
    fun getUserFlow(): Flow<FirestoreUser?> = flow {
        val userId = currentUserId ?: run {
            emit(null)
            return@flow
        }
        
        try {
            val snapshot = usersCollection()
                .document(userId)
                .get()
                .await()
            emit(snapshot.toObject(FirestoreUser::class.java))
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "User flow hatası", e)
            emit(null)
        }
    }
}
