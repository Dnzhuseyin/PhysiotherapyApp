package com.example.physiotherapyapp.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.physiotherapyapp.data.Exercise
import com.example.physiotherapyapp.data.Session
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
    
    // Mevcut seans bilgileri
    private val _currentSession = mutableStateOf<Session?>(null)
    val currentSession = _currentSession
    
    // Mevcut egzersiz index'i
    private val _currentExerciseIndex = mutableStateOf(0)
    val currentExerciseIndex = _currentExerciseIndex
    
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
        Exercise(name = "Ayak Bileği Dönme", description = "Ayak bileği esneklik egzersizi")
    )
    
    /**
     * Seçilen egzersizlerle yeni seans başlatır
     */
    fun startSession(selectedExercises: List<Exercise>) {
        val session = Session(
            exercises = selectedExercises,
            startDate = Date()
        )
        _currentSession.value = session
        _currentExerciseIndex.value = 0
    }
    
    /**
     * Mevcut egzersizi tamamlar ve bir sonrakine geçer
     */
    fun completeCurrentExercise() {
        val session = _currentSession.value ?: return
        val currentIndex = _currentExerciseIndex.value
        
        if (currentIndex < session.exercises.size) {
            // Mevcut egzersizi tamamlanmış olarak işaretle
            val updatedExercises = session.exercises.mapIndexed { index, exercise ->
                if (index == currentIndex) exercise.copy(isCompleted = true) else exercise
            }
            
            // Seansı güncelle
            _currentSession.value = session.copy(exercises = updatedExercises)
            
            // Sonraki egzersize geç
            _currentExerciseIndex.value = currentIndex + 1
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
        _currentExerciseIndex.value = 0
    }
    
    /**
     * Mevcut seansı iptal eder
     */
    fun cancelSession() {
        _currentSession.value = null
        _currentExerciseIndex.value = 0
    }
    
    /**
     * Mevcut egzersizin tamamlanıp tamamlanmadığını kontrol eder
     */
    fun isCurrentExerciseCompleted(): Boolean {
        val session = _currentSession.value ?: return false
        val currentIndex = _currentExerciseIndex.value
        return currentIndex < session.exercises.size && 
               session.exercises[currentIndex].isCompleted
    }
    
    /**
     * Tüm egzersizlerin tamamlanıp tamamlanmadığını kontrol eder
     */
    fun areAllExercisesCompleted(): Boolean {
        val session = _currentSession.value ?: return false
        return _currentExerciseIndex.value >= session.exercises.size
    }
} 