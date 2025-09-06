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
            val userId = currentUserId ?: run {
                android.util.Log.w("FirebaseRepo", "getUser: currentUserId is null")
                return null
            }
            android.util.Log.d("FirebaseRepo", "getUser: userId=$userId")
            
            val document = usersCollection()
                .document(userId)
                .get()
                .await()
                
            android.util.Log.d("FirebaseRepo", "getUser: document exists=${document.exists()}")
            
            val result = document.toObject(FirestoreUser::class.java)
            android.util.Log.d("FirebaseRepo", "getUser: result=$result")
            result
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
        // Temporarily disabled - conversion issues
        android.util.Log.w("FirebaseRepo", "saveSessionTemplate: Temporarily disabled")
        return false
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
            val userId = currentUserId ?: run {
                android.util.Log.w("FirebaseRepo", "savePainEntry: currentUserId is null")
                return false
            }
            android.util.Log.d("FirebaseRepo", "savePainEntry: Saving pain entry for userId: $userId")
            android.util.Log.d("FirebaseRepo", "savePainEntry: PainEntry - SessionId: ${painEntry.sessionId}, Level: ${painEntry.painLevel}, BodyPart: ${painEntry.bodyPart}")
            
            val firestorePainEntry = painEntryToFirestore(painEntry, userId)
            android.util.Log.d("FirebaseRepo", "savePainEntry: Converted to Firestore format - userId: ${firestorePainEntry.userId}")
            
            val documentRef = painEntriesCollection().add(firestorePainEntry).await()
            android.util.Log.d("FirebaseRepo", "savePainEntry: Successfully saved with document ID: ${documentRef.id}")
            
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "savePainEntry: Ağrı kaydı kaydetme hatası", e)
            android.util.Log.e("FirebaseRepo", "savePainEntry: Error details - ${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }
    
    /**
     * Kullanıcının ağrı kayıtlarını getir
     */
    suspend fun getUserPainEntries(): List<PainEntry> {
        return try {
            val userId = currentUserId ?: run {
                android.util.Log.w("FirebaseRepo", "getUserPainEntries: currentUserId is null")
                return emptyList()
            }
            android.util.Log.d("FirebaseRepo", "getUserPainEntries: Fetching pain entries for userId: $userId")
            android.util.Log.d("FirebaseRepo", "getUserPainEntries: Collection path: ${painEntriesCollection().path}")
            
            // Önce tüm collection'ı çek - debug için
            val allDocuments = painEntriesCollection().get().await()
            android.util.Log.d("FirebaseRepo", "getUserPainEntries: Total documents in collection: ${allDocuments.size()}")
            
            for (doc in allDocuments.documents) {
                android.util.Log.d("FirebaseRepo", "getUserPainEntries: Document ${doc.id} - userId: ${doc.getString("userId")}")
            }
            
            // OrderBy kaldırıldı - Firestore index sorunu olabilir
            val querySnapshot = painEntriesCollection()
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            android.util.Log.d("FirebaseRepo", "getUserPainEntries: Query with userId filter returned ${querySnapshot.size()} documents")
            
            val firestoreEntries = querySnapshot.toObjects(FirestorePainEntry::class.java)
            android.util.Log.d("FirebaseRepo", "getUserPainEntries: Converted to ${firestoreEntries.size} FirestorePainEntry objects")
            
            val localEntries = firestoreEntries.map { it.toLocal() }
            android.util.Log.d("FirebaseRepo", "getUserPainEntries: Final result: ${localEntries.size} PainEntry objects")
            
            return localEntries
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "getUserPainEntries: Ağrı kayıtları getirme hatası", e)
            android.util.Log.e("FirebaseRepo", "getUserPainEntries: Error details - ${e.javaClass.simpleName}: ${e.message}")
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
    
    // Eski badge operations kaldırıldı - aşağıda yeni versiyonlar var
    
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
    
    // Eski reminder operations kaldırıldı - aşağıda yeni versiyonlar var
    
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
    
    // BADGE OPERATIONS
    
    /**
     * Kullanıcının rozetlerini getir
     */
    suspend fun getUserBadges(): List<Badge> {
        return try {
            val userId = currentUserId ?: run {
                android.util.Log.w("FirebaseRepo", "getUserBadges: currentUserId is null")
                return emptyList()
            }
            android.util.Log.d("FirebaseRepo", "getUserBadges: Fetching badges for userId: $userId")
            
            val querySnapshot = badgesCollection()
                .whereEqualTo("userId", userId)
                .whereEqualTo("isUnlocked", true)
                .get()
                .await()
            
            android.util.Log.d("FirebaseRepo", "getUserBadges: Query returned ${querySnapshot.size()} documents")
            
            val badges = querySnapshot.documents.map { document ->
                android.util.Log.d("FirebaseRepo", "getUserBadges: Processing badge document: ${document.id}")
                Badge(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    description = document.getString("description") ?: "",
                    iconName = document.getString("iconName") ?: "",
                    category = try {
                        BadgeCategory.valueOf(document.getString("badgeType") ?: "SESSIONS")
                    } catch (e: Exception) {
                        BadgeCategory.SESSIONS
                    },
                    unlockedDate = document.getTimestamp("unlockedAt")?.toDate() ?: java.util.Date()
                )
            }
            
            android.util.Log.d("FirebaseRepo", "getUserBadges: Final result: ${badges.size} Badge objects")
            return badges
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "getUserBadges: Rozetler getirme hatası", e)
            android.util.Log.e("FirebaseRepo", "getUserBadges: Error details - ${e.javaClass.simpleName}: ${e.message}")
            emptyList()
        }
    }
    
    // REMINDERS OPERATIONS
    
    /**
     * Kullanıcının hatırlatıcılarını getir
     */
    suspend fun getUserReminders(): List<ReminderNotification> {
        return try {
            val userId = currentUserId ?: return emptyList()
            remindersCollection()
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .map { document ->
                    ReminderNotification(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        message = document.getString("message") ?: "",
                        time = document.getString("time") ?: "",
                        daysOfWeek = (document.get("daysOfWeek") as? List<Long>)?.map { it.toInt() } ?: emptyList(),
                        isEnabled = document.getBoolean("isEnabled") ?: true
                    )
                }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Hatırlatıcılar getirme hatası", e)
            emptyList()
        }
    }
    
    // PROGRESS REPORT OPERATIONS
    
    /**
     * Progress raporu kaydet
     */
    suspend fun saveProgressReport(report: ProgressReport): Boolean {
        return try {
            val userId = currentUserId ?: return false
            
            // Progress report'u Firestore'a kaydet
            progressReportsCollection()
                .document("${userId}_${System.currentTimeMillis()}")
                .set(mapOf(
                    "userId" to userId,
                    "startDate" to report.startDate,
                    "endDate" to report.endDate,
                    "totalSessions" to report.totalSessions,
                    "totalPoints" to report.totalPoints,
                    "avgPainLevel" to report.avgPainLevel,
                    "mostFrequentExercises" to report.mostFrequentExercises,
                    "createdAt" to com.google.firebase.Timestamp.now()
                ))
                .await()
                
            android.util.Log.d("FirebaseRepo", "Progress report saved successfully")
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Progress report kaydetme hatası", e)
            false
        }
    }
}
