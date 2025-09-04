package com.example.physiotherapyapp.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physiotherapyapp.data.*
import com.example.physiotherapyapp.services.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Fizik tedavi uygulamasının ana ViewModel'i
 * Tüm veri yönetimi ve iş mantığı burada gerçekleşir
 */
class PhysiotherapyViewModel(
    private val context: Context? = null
) : ViewModel() {
    
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
    
    // Sesli yönlendirme servisi
    private var voiceGuidanceService: VoiceGuidanceService? = null
    private val _voiceSettings = mutableStateOf(VoiceSettings())
    val voiceSettings = _voiceSettings
    
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
    
    // AI servisi
    private val aiService = AIRecommendationService()
    
    // Kullanıcı profili
    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile = _userProfile
    
    // AI önerileri
    private val _aiRecommendations = mutableStateListOf<AISessionRecommendation>()
    val aiRecommendations: List<AISessionRecommendation> = _aiRecommendations
    
    // Önceden tanımlanmış egzersizler listesi
    val availableExercises = listOf(
        Exercise(name = "Kol Kaldırma", description = "Kolları yanlara doğru kaldırma egzersizi"),
        Exercise(name = "Diz Bükme", description = "Dizleri bükerek esneklik kazandırma"),
        Exercise(name = "Omuz Dönme", description = "Omuzları döndürme hareketi"),
        Exercise(name = "Boyun Esneme", description = "Boyun kaslarını esneten hareketler"),
        Exercise(name = "Bel Esneme", description = "Bel kaslarını rahatlatma egzersizi"),
        Exercise(name = "Ayak Bileği Dönme", description = "Ayak bileği esneklik egzersizi"),
        Exercise(name = "Sırt Germe", description = "Sırt kaslarını germe egzersizi"),
        Exercise(name = "Kalça Hareketleri", description = "Kalça esnekliği için hareketler")
    )
    
    init {
        // Örnek seans şablonları oluştur
        createSampleTemplates()
        
        // Sesli yönlendirme servisini başlat
        initializeVoiceGuidance()
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
     * Sesli yönlendirme servisini başlatır
     */
    private fun initializeVoiceGuidance() {
        context?.let {
            voiceGuidanceService = VoiceGuidanceService(it)
        }
    }
    
    /**
     * Yeni seans şablonu oluşturur
     */
    fun createSessionTemplate(name: String, selectedExercises: List<Exercise>) {
        val template = SessionTemplate(
            name = name,
            exercises = selectedExercises
        )
        _sessionTemplates.add(template)
    }
    
    /**
     * Seans şablonunu siler
     */
    fun deleteSessionTemplate(templateId: String) {
        _sessionTemplates.removeAll { it.id == templateId }
    }
    
    /**
     * Şablondan yeni seans başlatır
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
        
        // Sesli duyuru: Seans başlangıcı
        if (_voiceSettings.value.announceStart) {
            voiceGuidanceService?.speak("${template.name} seansına hoş geldiniz. " +
                    "${template.exercises.size} egzersiz ile sağlıklı bir seans geçireceksiniz.")
            
            viewModelScope.launch {
                delay(3000) // 3 saniye bekle
                val firstExercise = session.exercises.firstOrNull()
                firstExercise?.let {
                    voiceGuidanceService?.announceExerciseStart(it.name)
                    if (_voiceSettings.value.giveInstructions) {
                        delay(2000)
                        voiceGuidanceService?.giveExerciseInstruction(it.name)
                    }
                }
            }
        }
    }
    
    /**
     * Mevcut egzersizi tamamlar ve bir sonrakine geçer
     */
    fun completeCurrentExercise() {
        val session = _currentSession.value ?: return
        val currentIndex = session.currentExerciseIndex
        
        if (currentIndex < session.exercises.size) {
            val currentExercise = session.exercises[currentIndex]
            
            // Mevcut egzersizi tamamlanmış olarak işaretle
            val updatedExercises = session.exercises.mapIndexed { index, exercise ->
                if (index == currentIndex) exercise.copy(isCompleted = true) else exercise
            }
            
            // Seansı güncelle - sonraki egzersize geç
            _currentSession.value = session.copy(
                exercises = updatedExercises,
                currentExerciseIndex = currentIndex + 1
            )
            
            // Önce önceki sesi durdur
            voiceGuidanceService?.stopSpeaking()
            
            // Sesli duyuru: Egzersiz tamamlandı
            if (_voiceSettings.value.announceComplete) {
                viewModelScope.launch {
                    // Kısa bir bekletme sonrası tebrik mesajı
                    delay(500)
                    voiceGuidanceService?.announceExerciseComplete(currentExercise.name)
                    
                    // Motivasyon mesajı (her 2 egzersizde bir)
                    if (_voiceSettings.value.motivationalMessages && currentIndex % 2 == 1) {
                        delay(2000)
                        voiceGuidanceService?.giveMotivation()
                    }
                    
                                    // Sonraki egzersiz duyurusu (sadece daha fazla egzersiz varsa)
                val nextIndex = currentIndex + 1
                if (nextIndex < session.exercises.size) {
                    val nextExercise = session.exercises[nextIndex]
                    delay(if (_voiceSettings.value.motivationalMessages && currentIndex % 2 == 1) 3000 else 2500)
                    voiceGuidanceService?.announceNextExercise(nextExercise.name)
                    
                    if (_voiceSettings.value.giveInstructions) {
                        delay(2500)
                        voiceGuidanceService?.giveExerciseInstruction(nextExercise.name)
                    }
                } else {
                    // Son egzersiz tamamlandı, sesli komutu durdur
                    delay(3000)
                    voiceGuidanceService?.speak("Tüm egzersizleri tamamladınız! Seans bitirmek için butona basın.")
                }
                }
            }
        }
    }
    
    /**
     * Tüm seansı tamamlar ve kullanıcı bilgilerini günceller
     */
    fun completeSession() {
        val session = _currentSession.value ?: return
        
        // Seansı tamamlanmış olarak işaretle ve 10 puan ekle
        val completedSession = session.copy(
            isCompleted = true,
            endDate = Date(),
            pointsEarned = 10
        )
        
        // Tamamlanan seanslar listesine ekle
        _completedSessions.add(completedSession)
        
        // Kullanıcı bilgilerini güncelle
        val updatedUser = _user.value.copy(
            totalSessions = _user.value.totalSessions + 1,
            totalPoints = _user.value.totalPoints + 10,
            completedSessions = _completedSessions.toList(),
            painEntries = _painEntries.toList()
        )
        _user.value = updatedUser
        
        // Yeni rozetleri kontrol et
        checkAndAwardBadges(updatedUser)
        
        // Sesli duyuru: Seans tamamlandı
        if (_voiceSettings.value.announceComplete) {
            viewModelScope.launch {
                voiceGuidanceService?.announceSessionComplete(
                    completedSession.templateName,
                    completedSession.exercises.size
                )
                // 5 saniye sonra sesi durdur
                delay(5000)
                voiceGuidanceService?.stopSpeaking()
            }
        }
        
        // Mevcut seans bilgilerini temizle
        _currentSession.value = null
    }
    
    /**
     * Mevcut seansı iptal eder
     */
    fun cancelSession() {
        // Sesli komutları durdur
        voiceGuidanceService?.stopSpeaking()
        _currentSession.value = null
    }
    
    /**
     * Mevcut egzersizin tamamlanıp tamamlanmadığını kontrol eder
     */
    fun isCurrentExerciseCompleted(): Boolean {
        val session = _currentSession.value ?: return false
        val currentIndex = session.currentExerciseIndex
        return currentIndex < session.exercises.size && 
               session.exercises[currentIndex].isCompleted
    }
    
    /**
     * Tüm egzersizlerin tamamlanıp tamamlanmadığını kontrol eder
     */
    fun areAllExercisesCompleted(): Boolean {
        val session = _currentSession.value ?: return false
        return session.currentExerciseIndex >= session.exercises.size
    }
    
    /**
     * Mevcut egzersizi getirir
     */
    fun getCurrentExercise(): Exercise? {
        val session = _currentSession.value ?: return null
        val currentIndex = session.currentExerciseIndex
        return if (currentIndex < session.exercises.size) {
            session.exercises[currentIndex]
        } else null
    }
    
    /**
     * Sesli yönlendirme ayarlarını günceller
     */
    fun updateVoiceSettings(settings: VoiceSettings) {
        _voiceSettings.value = settings
        voiceGuidanceService?.updateSettings(
            enabled = settings.isEnabled,
            rate = settings.speechRate,
            pitch = settings.speechPitch
        )
    }
    
    /**
     * Sesli konuşmayı durdurur
     */
    fun stopVoiceGuidance() {
        voiceGuidanceService?.stopSpeaking()
    }
    
    /**
     * Egzersiz talimatını tekrar söyler
     */
    fun repeatInstruction() {
        getCurrentExercise()?.let { exercise ->
            voiceGuidanceService?.giveExerciseInstruction(exercise.name)
        }
    }
    
    /**
     * Motivasyon mesajı söyler
     */
    fun giveMotivation() {
        voiceGuidanceService?.giveMotivation()
    }
    
    /**
     * Geri sayım başlatır
     */
    fun startCountdown(seconds: Int = 3) {
        voiceGuidanceService?.countdown(seconds)
    }
    
    /**
     * Ağrı günlüğü girdisi ekler
     */
    fun addPainEntry(painEntry: PainEntry) {
        _painEntries.add(painEntry)
        
        // Kullanıcıyı güncelle
        _user.value = _user.value.copy(
            painEntries = _painEntries.toList()
        )
        
        // Yeni rozetleri kontrol et
        checkAndAwardBadges(_user.value)
    }
    
    /**
     * Yeni rozetleri kontrol eder ve kazandırır
     */
    private fun checkAndAwardBadges(user: User) {
        val newlyEarnedBadges = badgeService.checkForNewBadges(user)
        if (newlyEarnedBadges.isNotEmpty()) {
            _newBadges.addAll(newlyEarnedBadges)
            
            // Kullanıcının rozet listesini güncelle
            _user.value = user.copy(
                badges = user.badges + newlyEarnedBadges
            )
        }
    }
    
    /**
     * Yeni rozet bildirimini temizler
     */
    fun clearNewBadge(badgeId: String) {
        _newBadges.removeAll { it.id == badgeId }
    }
    
    /**
     * Tüm yeni rozet bildirimlerini temizler
     */
    fun clearAllNewBadges() {
        _newBadges.clear()
    }
    
    /**
     * Hatırlatıcı ekler
     */
    fun addReminder(reminder: ReminderNotification) {
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
    fun deleteReminder(reminderId: String) {
        _reminders.removeAll { it.id == reminderId }
    }
    
    /**
     * Kişisel hedefleri günceller
     */
    fun updatePersonalGoals(goals: PersonalGoals) {
        _user.value = _user.value.copy(goals = goals)
    }
    
    /**
     * Günlük hedef ilerlemesini hesaplar
     */
    fun getDailyProgress(): DailyProgress {
        val today = Date()
        val todaySessions = _completedSessions.filter { 
            isSameDay(it.startDate, today)
        }
        
        val todayPoints = todaySessions.sumOf { it.pointsEarned }
        val goals = _user.value.goals
        
        return DailyProgress(
            sessionsCompleted = todaySessions.size,
            sessionTarget = goals.dailySessionTarget,
            pointsEarned = todayPoints,
            pointTarget = goals.dailyPointTarget
        )
    }
    
    /**
     * Haftalık hedef ilerlemesini hesaplar
     */
    fun getWeeklyProgress(): WeeklyProgress {
        val today = Date()
        val weekStart = getWeekStart(today)
        val weekSessions = _completedSessions.filter { 
            it.startDate >= weekStart && it.startDate <= today
        }
        
        val weekPoints = weekSessions.sumOf { it.pointsEarned }
        val avgPainLevel = if (_painEntries.isNotEmpty()) {
            _painEntries.filter { it.date >= weekStart }
                .map { it.painLevel }
                .average()
        } else 0.0
        
        return WeeklyProgress(
            weekStartDate = weekStart,
            sessionsCompleted = weekSessions.size,
            pointsEarned = weekPoints,
            avgPainLevel = avgPainLevel
        )
    }
    
    /**
     * İlerleme raporu oluşturur
     */
    fun generateProgressReport(startDate: Date, endDate: Date): ProgressReport {
        val periodSessions = _completedSessions.filter { 
            it.startDate >= startDate && it.startDate <= endDate
        }
        
        val periodPainEntries = _painEntries.filter {
            it.date >= startDate && it.date <= endDate
        }
        
        val exerciseFrequency = periodSessions
            .flatMap { it.exercises }
            .groupingBy { it.name }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
        
        return ProgressReport(
            startDate = startDate,
            endDate = endDate,
            totalSessions = periodSessions.size,
            totalPoints = periodSessions.sumOf { it.pointsEarned },
            avgPainLevel = if (periodPainEntries.isNotEmpty()) {
                periodPainEntries.map { it.painLevel }.average()
            } else 0.0,
            mostFrequentExercises = exerciseFrequency,
            weeklyProgress = generateWeeklyProgressList(startDate, endDate)
        )
    }
    
    /**
     * Haftalık ilerleme listesi oluşturur
     */
    private fun generateWeeklyProgressList(startDate: Date, endDate: Date): List<WeeklyProgress> {
        val weeklyProgress = mutableListOf<WeeklyProgress>()
        var currentWeekStart = getWeekStart(startDate)
        
        while (currentWeekStart <= endDate) {
            val weekEnd = Date(currentWeekStart.time + 7 * 24 * 60 * 60 * 1000)
            val weekSessions = _completedSessions.filter { 
                it.startDate >= currentWeekStart && it.startDate < weekEnd
            }
            
            val weekPainEntries = _painEntries.filter {
                it.date >= currentWeekStart && it.date < weekEnd
            }
            
            weeklyProgress.add(
                WeeklyProgress(
                    weekStartDate = currentWeekStart,
                    sessionsCompleted = weekSessions.size,
                    pointsEarned = weekSessions.sumOf { it.pointsEarned },
                    avgPainLevel = if (weekPainEntries.isNotEmpty()) {
                        weekPainEntries.map { it.painLevel }.average()
                    } else 0.0
                )
            )
            
            currentWeekStart = weekEnd
        }
        
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
     * Kullanıcı profilini ayarlar
     */
    fun setUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        
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
                val currentPain = _painEntries.lastOrNull()?.painLevel
                val recentSessions = _completedSessions.takeLast(5).map { it.templateName }
                
                val recommendation = aiService.generateSessionRecommendation(
                    userProfile = profile,
                    currentPainLevel = currentPain,
                    previousSessions = recentSessions
                )
                
                _aiRecommendations.clear()
                _aiRecommendations.add(recommendation)
                
            } catch (e: Exception) {
                // Hata durumunda sessizce fallback yap
                e.printStackTrace()
            }
        }
    }
    
    /**
     * AI önerisini SessionTemplate olarak ekler
     */
    fun acceptAIRecommendation(recommendation: AISessionRecommendation) {
        val exercises = recommendation.exercises.map { exerciseName ->
            availableExercises.find { it.name == exerciseName } 
                ?: Exercise(name = exerciseName, description = "AI önerisi")
        }
        
        val template = SessionTemplate(
            name = recommendation.sessionName,
            exercises = exercises,
            estimatedDuration = recommendation.estimatedDuration
        )
        
        _sessionTemplates.add(template)
    }
    
    /**
     * SessionTemplate ekler
     */
    fun addSessionTemplate(template: SessionTemplate) {
        _sessionTemplates.add(template)
    }
    
    /**
     * Ağrı girdisini düzenler
     */
    fun updatePainEntry(updatedEntry: PainEntry) {
        val index = _painEntries.indexOfFirst { it.id == updatedEntry.id }
        if (index != -1) {
            _painEntries[index] = updatedEntry
            
            // Kullanıcıyı güncelle
            _user.value = _user.value.copy(
                painEntries = _painEntries.toList()
            )
        }
    }
    
    /**
     * Ağrı girdisini siler
     */
    fun deletePainEntry(entryId: String) {
        _painEntries.removeAll { it.id == entryId }
        
        // Kullanıcıyı güncelle
        _user.value = _user.value.copy(
            painEntries = _painEntries.toList()
        )
    }
    
    /**
     * Kişiselleştirilmiş egzersiz açıklaması al
     */
    suspend fun getPersonalizedExerciseDescription(exerciseName: String): String {
        val profile = _userProfile.value
        val currentPain = _painEntries.lastOrNull()
        
        return if (profile != null) {
            try {
                aiService.getPersonalizedExerciseDescription(
                    exerciseName = exerciseName,
                    userProfile = profile,
                    currentCondition = currentPain?.let { "Ağrı seviyesi: ${it.painLevel}/10" }
                )
            } catch (e: Exception) {
                getDefaultExerciseDescription(exerciseName)
            }
        } else {
            getDefaultExerciseDescription(exerciseName)
        }
    }
    
    /**
     * Varsayılan egzersiz açıklaması
     */
    private fun getDefaultExerciseDescription(exerciseName: String): String {
        return availableExercises.find { it.name == exerciseName }?.description 
            ?: "Bu egzersizi doğru form ile yapın ve nefes almayı unutmayın."
    }
    
    /**
     * ViewModel temizleme
     */
    override fun onCleared() {
        super.onCleared()
        voiceGuidanceService?.shutdown()
    }
}

/**
 * Günlük ilerleme data class'ı
 */
data class DailyProgress(
    val sessionsCompleted: Int,
    val sessionTarget: Int,
    val pointsEarned: Int,
    val pointTarget: Int
) {
    val sessionProgress: Float = 
        if (sessionTarget > 0) sessionsCompleted.toFloat() / sessionTarget else 0f
    val pointProgress: Float = 
        if (pointTarget > 0) pointsEarned.toFloat() / pointTarget else 0f
} 