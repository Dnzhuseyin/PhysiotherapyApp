package com.example.physiotherapyapp.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physiotherapyapp.data.*
import com.example.physiotherapyapp.services.BadgeService
import com.example.physiotherapyapp.services.AIRecommendationService
import com.example.physiotherapyapp.repository.FirebaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

/**
 * Firebase entegrasyon ile temizlenmiş PhysiotherapyViewModel
 * Voice guidance kaldırıldı, Firebase entegrasyonu eklendi
 */
class PhysiotherapyViewModel(
    private val context: Context? = null
) : ViewModel() {
    
    // Firebase Repository
    private val firebaseRepository = FirebaseRepository()
    
    // Kullanıcı bilgileri
    private val _user = mutableStateOf(User())
    val user = _user
    
    // Mevcut aktif seans bilgileri
    private val _currentSession = mutableStateOf<Session?>(null)
    val currentSession = _currentSession
    
    // Kayıtlı seans şablonları
    private val _sessionTemplates = mutableStateListOf<SessionTemplate>()
    val sessionTemplates: List<SessionTemplate> = _sessionTemplates
    
    // Tamamlanan seanslar listesi
    private val _completedSessions = mutableStateListOf<Session>()
    val completedSessions: List<Session> = _completedSessions
    
    // Rozet servisi
    private val badgeService = BadgeService()
    private val _newBadges = mutableStateListOf<Badge>()
    val newBadges: List<Badge> = _newBadges
    
    // Ağrı günlüğü
    private val _painEntries = mutableStateListOf<PainEntry>()
    val painEntries: List<PainEntry> = _painEntries
    
    // Hatırlatıcılar
    private val _reminders = mutableStateListOf<ReminderNotification>()
    val reminders: List<ReminderNotification> = _reminders
    
    // AI Servisi
    private val aiService = AIRecommendationService()
    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile = _userProfile
    private val _aiRecommendations = mutableStateOf<List<AISessionRecommendation>>(emptyList())
    val aiRecommendations = _aiRecommendations
    
    // Mevcut kullanılabilir egzersizler
    val availableExercises = listOf(
        Exercise(name = "Kol Kaldırma", description = "Kolları omuz hizasına kadar kaldırın"),
        Exercise(name = "Diz Bükme", description = "Dizleri göğse doğru çekin"),
        Exercise(name = "Omuz Dönme", description = "Omuzları dairesel hareket ettirin"),
        Exercise(name = "Boyun Esneme", description = "Boynu sağa sola eğin"),
        Exercise(name = "Bel Esneme", description = "Beli öne arkaya eğin"),
        Exercise(name = "Ayak Bileği Dönme", description = "Ayak bileği esneklik egzersizi"),
        Exercise(name = "Sırt Germe", description = "Sırt kaslarını germe egzersizi"),
        Exercise(name = "Kalça Hareketleri", description = "Kalça esnekliği için hareketler")
    )
    
    init {
        // Örnek seans şablonları oluştur (Firebase'de yoksa)
        createSampleTemplates()
        
        // Firebase'den veri yükle
        loadDataFromFirebase()
    }
    
    /**
     * Örnek seans şablonları oluşturur
     */
    private fun createSampleTemplates() {
        val morningTemplate = SessionTemplate(
            name = "Sabah Rutin",
            exercises = listOf(
                availableExercises[0], // Kol Kaldırma
                availableExercises[1], // Diz Bükme
                availableExercises[3]  // Boyun Esneme
            )
        )
        
        val eveningTemplate = SessionTemplate(
            name = "Akşam Seansı",
            exercises = listOf(
                availableExercises[2], // Omuz Dönme
                availableExercises[4], // Bel Esneme
                availableExercises[5]  // Ayak Bileği Dönme
            )
        )
        
        _sessionTemplates.addAll(listOf(morningTemplate, eveningTemplate))
    }
    
    /**
     * Firebase'den tüm kullanıcı verilerini yükler
     */
    fun loadDataFromFirebase() {
        android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Starting data load...")
        
        viewModelScope.launch {
            try {
                android.util.Log.d("PhysiotherapyViewModel", "==================== DATA LOAD START ====================")
                android.util.Log.d("PhysiotherapyViewModel", "Firebase Auth User: ${com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid}")
                android.util.Log.d("PhysiotherapyViewModel", "Local User BEFORE: ${_user.value.name}, Sessions: ${_user.value.totalSessions}")
                android.util.Log.d("PhysiotherapyViewModel", "Local Pain Entries BEFORE: ${_painEntries.size}")
                android.util.Log.d("PhysiotherapyViewModel", "Local Badges BEFORE: ${_user.value.badges.size}")
                
                // Kullanıcı bilgilerini yükle
                val existingUser = firebaseRepository.getUser()
                if (existingUser != null) {
                    android.util.Log.d("PhysiotherapyViewModel", "User loaded from Firebase: ${existingUser.uid}")
                    _user.value = existingUser.toLocal()
                } else {
                    android.util.Log.w("PhysiotherapyViewModel", "No user data found, creating initial user")
                    // Yeni kullanıcı için initial data oluştur
                    createInitialUserData()
                }
                
                // Kullanıcı profilini yükle
                firebaseRepository.getUserProfile()?.let { profile ->
                    android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Profile loaded: ${profile.category}")
                    _userProfile.value = profile
                } ?: android.util.Log.w("PhysiotherapyViewModel", "loadDataFromFirebase: No profile found")
                
                // Session template'leri yükle
                val templates = firebaseRepository.getUserSessionTemplates()
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Templates loaded: ${templates.size}")
                
                // Template'leri sadece Firebase'den veri varsa güncelle, yoksa mevcut local template'leri koru
                if (templates.isNotEmpty()) {
                    _sessionTemplates.clear()
                    _sessionTemplates.addAll(templates)
                } else if (_sessionTemplates.isEmpty()) {
                    // Sadece local template'ler de boşsa sample template'ler oluştur
                    createSampleTemplates()
                }
                
                // Tamamlanan seansları yükle
                val completedSessions = firebaseRepository.getUserCompletedSessions()
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Sessions loaded: ${completedSessions.size}")
                _user.value = _user.value.copy(completedSessions = completedSessions)
                
                // Local completedSessions listesini de güncelle
                _completedSessions.clear()
                _completedSessions.addAll(completedSessions)
                
                // Ağrı kayıtlarını yükle
                val painEntries = firebaseRepository.getUserPainEntries()
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Pain entries loaded: ${painEntries.size}")
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Current local pain entries: ${_painEntries.size}")
                
                _user.value = _user.value.copy(painEntries = painEntries)
                
                // Local painEntries listesini sadece Firebase'den veri varsa güncelle
                if (painEntries.isNotEmpty() || _painEntries.isEmpty()) {
                    android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Updating local pain entries list")
                    _painEntries.clear()
                    _painEntries.addAll(painEntries)
                    android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Local pain entries after update: ${_painEntries.size}")
                } else {
                    android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Keeping existing local pain entries")
                }
                
                // Rozetleri yükle
                val badges = firebaseRepository.getUserBadges()
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Badges loaded from Firebase: ${badges.size}")
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Current user badges: ${_user.value.badges.size}")
                
                _user.value = _user.value.copy(badges = badges)
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: User badges after update: ${_user.value.badges.size}")
                
                // Hatırlatıcıları yükle
                val reminders = firebaseRepository.getUserReminders()
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Reminders loaded: ${reminders.size}")
                
                // Local reminders listesini sadece Firebase'den veri varsa güncelle
                if (reminders.isNotEmpty() || _reminders.isEmpty()) {
                    _reminders.clear()
                    _reminders.addAll(reminders)
                }
                
                android.util.Log.d("PhysiotherapyViewModel", "loadDataFromFirebase: Data load completed successfully")
                
            } catch (e: Exception) {
                android.util.Log.e("PhysiotherapyViewModel", "Firebase veri yükleme hatası", e)
            }
        }
    }
    
    /**
     * Yeni kullanıcı için initial data oluşturur ve Firebase'e kaydeder
     */
    private suspend fun createInitialUserData() {
        try {
            // Firebase Auth'dan kullanıcı bilgilerini al
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            // Varsayılan kullanıcı bilgileri oluştur
            val defaultUser = User(
                name = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Kullanıcı",
                totalSessions = 0,
                totalPoints = 0,
                completedSessions = emptyList(),
                painEntries = emptyList(),
                badges = emptyList(),
                goals = PersonalGoals()
            )
            
            // Local state'i güncelle
            _user.value = defaultUser
            
            // Firebase'e kaydet
            firebaseRepository.updateUser(defaultUser)
            
            android.util.Log.d("PhysiotherapyViewModel", "Initial user data created and saved to Firebase: ${defaultUser.name}")
            
        } catch (e: Exception) {
            android.util.Log.e("PhysiotherapyViewModel", "Error creating initial user data", e)
        }
    }
    
    /**
     * Kullanıcı verilerini Firebase'e senkronize et
     */
    private fun syncUserToFirebase() {
        viewModelScope.launch {
            try {
                firebaseRepository.updateUser(_user.value)
            } catch (e: Exception) {
                android.util.Log.e("PhysiotherapyViewModel", "Firebase senkronizasyon hatası", e)
            }
        }
    }
    
    /**
     * Yeni seans şablonu oluşturur ve Firebase'e kaydeder
     */
    fun createSessionTemplate(name: String, selectedExercises: List<Exercise>) {
        val template = SessionTemplate(
            name = name,
            exercises = selectedExercises
        )
        _sessionTemplates.add(template)
        
        // Firebase'e kaydet
        viewModelScope.launch {
            firebaseRepository.saveSessionTemplate(template)
        }
    }
    
    /**
     * Seans şablonunu siler
     */
    fun deleteSessionTemplate(templateId: String) {
        _sessionTemplates.removeAll { it.id == templateId }
        
        // Firebase'den de sil
        viewModelScope.launch {
            firebaseRepository.deleteSessionTemplate(templateId)
        }
    }
    
    /**
     * Şablondan seans başlatır
     */
    fun startSessionFromTemplate(template: SessionTemplate) {
        val session = Session(
            templateId = template.id,
            templateName = template.name,
            exercises = template.exercises.map { it.copy(isCompleted = false) },
            startDate = Date(),
            currentExerciseIndex = 0
        )
        _currentSession.value = session
    }
    
    /**
     * Mevcut egzersizi tamamlar ve bir sonrakine geçer
     */
    fun completeCurrentExercise() {
        val session = _currentSession.value ?: return
        
        // Mevcut egzersizi tamamla
        val updatedExercises = session.exercises.mapIndexed { index, exercise ->
            if (index == session.currentExerciseIndex) {
                exercise.copy(isCompleted = true)
            } else {
                exercise
            }
        }
        
        // Sessionu güncelle
        val updatedSession = session.copy(
            exercises = updatedExercises,
            currentExerciseIndex = session.currentExerciseIndex + 1
        )
        
        _currentSession.value = updatedSession
    }
    
    /**
     * Seans iptal edilir
     */
    fun cancelSession() {
        _currentSession.value = null
    }
    
    /**
     * Mevcut egzersizin tamamlanıp tamamlanmadığını kontrol eder
     */
    fun isCurrentExerciseCompleted(): Boolean {
        val session = _currentSession.value ?: return false
        return session.exercises.getOrNull(session.currentExerciseIndex)?.isCompleted == true
    }
    
    /**
     * Tüm egzersizlerin tamamlanıp tamamlanmadığını kontrol eder
     */
    fun areAllExercisesCompleted(): Boolean {
        val session = _currentSession.value ?: return false
        return session.currentExerciseIndex >= session.exercises.size
    }
    
    /**
     * Seansı tamamlar ve istatistikleri günceller
     */
    fun completeSession(): Session? {
        val session = _currentSession.value ?: return null
        
        // Seansı tamamla
        val completedSession = session.copy(
            isCompleted = true,
            endDate = Date(),
            pointsEarned = 10
        )
        
        // Tamamlanan seanslara ekle
        _completedSessions.add(completedSession)
        
        // Kullanıcı istatistiklerini güncelle
        _user.value = _user.value.copy(
            totalSessions = _user.value.totalSessions + 1,
            totalPoints = _user.value.totalPoints + 10,
            completedSessions = _user.value.completedSessions + completedSession
        )
        
        // Rozet kontrolü yap
        checkAndAwardBadges()
        
        // Mevcut seansı temizle
        _currentSession.value = null
        
        // Firebase'e kaydet
        viewModelScope.launch {
            firebaseRepository.saveCompletedSession(completedSession)
            syncUserToFirebase()
        }
        
        return completedSession
    }
    
    /**
     * Ağrı kaydı ekler
     */
    fun addPainEntry(sessionId: String, painLevel: Int, bodyPart: String, notes: String) {
        android.util.Log.d("PhysiotherapyViewModel", "==================== ADDING PAIN ENTRY ====================")
        android.util.Log.d("PhysiotherapyViewModel", "addPainEntry: SessionId: $sessionId, PainLevel: $painLevel, BodyPart: $bodyPart")
        android.util.Log.d("PhysiotherapyViewModel", "addPainEntry: Local pain entries BEFORE: ${_painEntries.size}")
        
        val painEntry = PainEntry(
            sessionId = sessionId,
            painLevel = painLevel,
            bodyPart = bodyPart,
            notes = notes
        )
        
        android.util.Log.d("PhysiotherapyViewModel", "addPainEntry: Created PainEntry with ID: ${painEntry.id}")
        
        _painEntries.add(painEntry)
        _user.value = _user.value.copy(painEntries = _user.value.painEntries + painEntry)
        
        android.util.Log.d("PhysiotherapyViewModel", "addPainEntry: Local pain entries AFTER: ${_painEntries.size}")
        android.util.Log.d("PhysiotherapyViewModel", "addPainEntry: User pain entries AFTER: ${_user.value.painEntries.size}")
        
        // Firebase'e kaydet
        viewModelScope.launch {
            try {
                android.util.Log.d("PhysiotherapyViewModel", "addPainEntry: Saving to Firebase...")
                val result = firebaseRepository.savePainEntry(painEntry)
                android.util.Log.d("PhysiotherapyViewModel", "addPainEntry: Firebase save result: $result")
            } catch (e: Exception) {
                android.util.Log.e("PhysiotherapyViewModel", "addPainEntry: Firebase save error", e)
            }
        }
    }
    
    /**
     * Ağrı kaydını düzenle
     */
    fun editPainEntry(painEntry: PainEntry, newPainLevel: Int, newNotes: String) {
        val updatedEntry = painEntry.copy(
            painLevel = newPainLevel,
            notes = newNotes
        )
        
        val index = _painEntries.indexOfFirst { it.id == painEntry.id }
        if (index != -1) {
            _painEntries[index] = updatedEntry
            
            // User'daki painEntries'i de güncelle
            val updatedPainEntries = _user.value.painEntries.map { entry ->
                if (entry.id == painEntry.id) updatedEntry else entry
            }
            _user.value = _user.value.copy(painEntries = updatedPainEntries)
            
            // Firebase'e kaydet
            viewModelScope.launch {
                firebaseRepository.updatePainEntry(updatedEntry)
            }
        }
    }
    
    /**
     * Ağrı kaydını sil
     */
    fun deletePainEntry(painEntry: PainEntry) {
        _painEntries.removeAll { it.id == painEntry.id }
        
        // User'daki painEntries'den de sil
        val updatedPainEntries = _user.value.painEntries.filter { it.id != painEntry.id }
        _user.value = _user.value.copy(painEntries = updatedPainEntries)
        
        // Firebase'den sil
        viewModelScope.launch {
            firebaseRepository.deletePainEntry(painEntry.id)
        }
    }
    
    /**
     * Rozet kontrolü yapar ve yeni rozetler verir
     */
    fun checkAndAwardBadges() {
        val user = _user.value
        val newBadges = badgeService.checkBadgeEligibility(
            totalSessions = user.totalSessions,
            totalPoints = user.totalPoints,
            consecutiveDays = user.goals.currentDailyStreak,
            currentBadges = user.badges
        )
        
        if (newBadges.isNotEmpty()) {
            _newBadges.addAll(newBadges)
            _user.value = _user.value.copy(badges = _user.value.badges + newBadges)
            
            // Firebase'e kaydet
            viewModelScope.launch {
                newBadges.forEach { badge ->
                    firebaseRepository.saveBadge(badge)
                }
                syncUserToFirebase()
            }
        }
    }
    
    /**
     * Yeni rozet bildirimini temizler
     */
    fun clearNewBadge(badge: Badge) {
        _newBadges.remove(badge)
    }
    
    /**
     * Hatırlatıcı ekler
     */
    fun addReminder(title: String, message: String, time: String, daysOfWeek: List<Int>) {
        val reminder = ReminderNotification(
            title = title,
            message = message,
            time = time,
            daysOfWeek = daysOfWeek
        )
        _reminders.add(reminder)
    }
    
    /**
     * Hatırlatıcı günceller
     */
    fun updateReminder(reminder: ReminderNotification) {
        val index = _reminders.indexOfFirst { it.id == reminder.id }
        if (index != -1) {
            _reminders[index] = reminder
        }
    }
    
    /**
     * Hatırlatıcı siler
     */
    fun deleteReminder(reminder: ReminderNotification) {
        _reminders.remove(reminder)
    }
    
    /**
     * Kişisel hedefleri günceller
     */
    fun updatePersonalGoals(goals: PersonalGoals) {
        _user.value = _user.value.copy(goals = goals)
        syncUserToFirebase()
    }
    
    /**
     * Günlük ilerleme verisi döndürür
     */
    fun getDailyProgress(): DailyProgress {
        val today = Date()
        val todaySessions = _user.value.completedSessions.filter { 
            isSameDay(it.startDate, today)
        }
        
        return DailyProgress(
            date = today,
            sessionsCompleted = todaySessions.size,
            pointsEarned = todaySessions.sumOf { it.pointsEarned },
            avgPainLevel = _painEntries.filter { isSameDay(it.date, today) }
                .takeIf { it.isNotEmpty() }
                ?.map { it.painLevel }
                ?.average() ?: 0.0
        )
    }
    
    /**
     * Haftalık ilerleme verisi döndürür
     */
    fun getWeeklyProgress(): WeeklyProgress {
        val weekStart = getWeekStart(Date())
        val weekSessions = _user.value.completedSessions.filter { 
            it.startDate >= weekStart
        }
        
        val weeklyProgress = WeeklyProgress(
            weekStartDate = weekStart,
            sessionsCompleted = weekSessions.size,
            pointsEarned = weekSessions.sumOf { it.pointsEarned },
            avgPainLevel = _painEntries.filter { it.date >= weekStart }
                .takeIf { it.isNotEmpty() }
                ?.map { it.painLevel }
                ?.average() ?: 0.0
        )
        
        return weeklyProgress
    }
    
    /**
     * İki tarihin aynı gün olup olmadığını kontrol eder
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Haftanın başlangıç tarihini döndürür (Pazartesi)
     */
    private fun getWeekStart(date: Date): Date {
        val cal = java.util.Calendar.getInstance().apply { 
            time = date
            firstDayOfWeek = java.util.Calendar.MONDAY
        }
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        
        return cal.time
    }
    
    /**
     * Kullanıcı profilini ayarlar ve Firebase'e kaydeder
     */
    fun setUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        
        // Firebase'e kaydet
        viewModelScope.launch {
            firebaseRepository.saveUserProfile(profile)
        }
        
        // Profil ayarlandığında AI önerileri al
        generateAIRecommendations()
    }
    
    /**
     * AI önerileri üretir
     */
    fun generateAIRecommendations() {
        val profile = _userProfile.value ?: return
        
        viewModelScope.launch {
            try {
                val currentPainLevel = _painEntries.lastOrNull()?.painLevel
                val previousSessions = _completedSessions.takeLast(5).map { it.templateName }
                
                val recommendation = aiService.generateSessionRecommendation(
                    userProfile = profile,
                    currentPainLevel = currentPainLevel,
                    previousSessions = previousSessions
                )
                
                _aiRecommendations.value = listOf(recommendation)
                
                // Firebase'e kaydet
                firebaseRepository.saveAIRecommendation(recommendation)
                
            } catch (e: Exception) {
                android.util.Log.e("AI", "Öneri oluşturma hatası", e)
            }
        }
    }
    
    /**
     * AI önerisini kabul eder ve seans şablonu oluşturur
     */
    fun acceptAIRecommendation(recommendation: AISessionRecommendation) {
        val exercises = recommendation.exercises.map { exerciseName ->
            Exercise(name = exerciseName, description = "")
        }
        
        createSessionTemplate(recommendation.sessionName, exercises)
    }
    
    /**
     * AI önerisinden seans şablonu oluşturur
     */
    fun addSessionTemplate(recommendation: AISessionRecommendation) {
        val exercises = recommendation.exercises.map { exerciseName ->
            availableExercises.find { it.name == exerciseName } 
                ?: Exercise(name = exerciseName, description = "")
        }
        
        createSessionTemplate(recommendation.sessionName, exercises)
    }
    
    /**
     * Egzersiz için kişiselleştirilmiş açıklama döndürür
     */
    fun getPersonalizedExerciseDescription(exerciseName: String): String {
        // Basit açıklama döndür - AI çağrısı fazla karmaşık
        return when (exerciseName) {
            "Kol Kaldırma" -> "Kollarınızı omuz hizasında yavaşça yukarı kaldırın ve indirin."
            "Diz Bükme" -> "Dizlerinizi göğsünüze doğru yavaşça çekin."
            "Omuz Dönme" -> "Omuzlarınızı dairesel hareket ettirerek gergin kasları gevşetin."
            "Boyun Esneme" -> "Boyununuzu sağa sola yavaşça eğin, ani hareketlerden kaçının."
            "Bel Esneme" -> "Ellerinizi belinize koyarak yavaşça geriye doğru eğilin."
            "Ayak Bileği Dönme" -> "Ayak bileğinizi saat yönünde ve ters yönde çevirin."
            "Sırt Germe" -> "Kollarınızı öne uzatarak sırt kaslarınızı gerin."
            "Kalça Hareketleri" -> "Kalçanızı yavaşça öne arkaya hareket ettirin."
            else -> "Bu egzersizi doğru form ile yapın ve nefes almayı unutmayın."
        }
    }
    
    /**
     * İlerleme raporu oluşturur ve Firebase'e kaydeder
     */
    fun generateProgressReport(startDate: Date, endDate: Date): ProgressReport {
        val sessionsInRange = _user.value.completedSessions.filter { session ->
            session.startDate >= startDate && session.startDate <= endDate
        }
        
        val painEntriesInRange = _painEntries.filter { entry ->
            entry.date >= startDate && entry.date <= endDate
        }
        
        val exerciseFrequency = sessionsInRange
            .flatMap { it.exercises }
            .groupBy { it.name }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
        
        val report = ProgressReport(
            startDate = startDate,
            endDate = endDate,
            totalSessions = sessionsInRange.size,
            totalPoints = sessionsInRange.sumOf { it.pointsEarned },
            avgPainLevel = painEntriesInRange.map { it.painLevel }.average(),
            mostFrequentExercises = exerciseFrequency,
            weeklyProgress = emptyList() // Haftalık detaylar için ayrı hesaplama gerekir
        )
        
        // Progress report'u Firebase'e kaydet
        viewModelScope.launch {
            try {
                firebaseRepository.saveProgressReport(report)
                android.util.Log.d("PhysiotherapyViewModel", "Progress report saved to Firebase")
            } catch (e: Exception) {
                android.util.Log.e("PhysiotherapyViewModel", "Error saving progress report to Firebase", e)
            }
        }
        
        return report
    }
    
    override fun onCleared() {
        super.onCleared()
        // Firebase connections otomatik temizlenir
    }
}

/**
 * Günlük ilerleme verisi
 */
data class DailyProgress(
    val date: Date,
    val sessionsCompleted: Int,
    val pointsEarned: Int,
    val avgPainLevel: Double,
    val sessionTarget: Int = 1,
    val pointTarget: Int = 10
) {
    val sessionProgress: Float
        get() = if (sessionTarget > 0) sessionsCompleted.toFloat() / sessionTarget else 0f
    val pointProgress: Float
        get() = if (pointTarget > 0) pointsEarned.toFloat() / pointTarget else 0f
}
