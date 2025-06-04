package com.example.physiotherapyapp.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.physiotherapyapp.data.Exercise
import com.example.physiotherapyapp.data.Session
import com.example.physiotherapyapp.data.SessionTemplate
import com.example.physiotherapyapp.data.User
import java.util.Date

/**
 * Fizik tedavi uygulamasının ana ViewModel'i
 * Tüm veri yönetimi ve iş mantığı burada gerçekleşir
 */
class PhysiotherapyViewModel : ViewModel() {
    
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
    }
    
    /**
     * Mevcut egzersizi tamamlar ve bir sonrakine geçer
     */
    fun completeCurrentExercise() {
        val session = _currentSession.value ?: return
        val currentIndex = session.currentExerciseIndex
        
        if (currentIndex < session.exercises.size) {
            // Mevcut egzersizi tamamlanmış olarak işaretle
            val updatedExercises = session.exercises.mapIndexed { index, exercise ->
                if (index == currentIndex) exercise.copy(isCompleted = true) else exercise
            }
            
            // Seansı güncelle - sonraki egzersize geç
            _currentSession.value = session.copy(
                exercises = updatedExercises,
                currentExerciseIndex = currentIndex + 1
            )
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
        _user.value = _user.value.copy(
            totalSessions = _user.value.totalSessions + 1,
            totalPoints = _user.value.totalPoints + 10,
            completedSessions = _completedSessions.toList()
        )
        
        // Mevcut seans bilgilerini temizle
        _currentSession.value = null
    }
    
    /**
     * Mevcut seansı iptal eder
     */
    fun cancelSession() {
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
} 