package com.example.physiotherapyapp.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/**
 * Sesli yönlendirme servisi
 * Egzersiz sırasında kullanıcıya sesli talimatlar verir
 */
class VoiceGuidanceService(private val context: Context) {
    
    private var textToSpeech: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking
    
    private var speechRate = 0.8f
    private var speechPitch = 1.0f
    private var isEnabled = true
    
    init {
        initializeTextToSpeech()
    }
    
    private fun initializeTextToSpeech() {
        android.util.Log.d("VoiceGuidance", "Initializing TTS...")
        textToSpeech = TextToSpeech(context) { status ->
            android.util.Log.d("VoiceGuidance", "TTS init status: $status")
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    android.util.Log.d("VoiceGuidance", "TTS init successful")
                    
                    // Türkçe dil desteği
                    val result = tts.setLanguage(Locale("tr", "TR"))
                    android.util.Log.d("VoiceGuidance", "Turkish language result: $result")
                    
                    if (result == TextToSpeech.LANG_MISSING_DATA || 
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        android.util.Log.d("VoiceGuidance", "Turkish not supported, trying English")
                        // Türkçe desteklenmiyorsa İngilizce deneyelim
                        val englishResult = tts.setLanguage(Locale.ENGLISH)
                        android.util.Log.d("VoiceGuidance", "English language result: $englishResult")
                        
                        if (englishResult == TextToSpeech.LANG_MISSING_DATA || 
                            englishResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            android.util.Log.d("VoiceGuidance", "English also not supported, using default")
                            tts.setLanguage(Locale.getDefault())
                        }
                    }
                    
                    // Konuşma hızı ve tonu ayarla
                    tts.setSpeechRate(speechRate)
                    tts.setPitch(speechPitch)
                    
                    // Progress listener ekle
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }
                        
                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                        
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                    })
                    
                    _isInitialized.value = true
                }
            }
        }
    }
    
    /**
     * Sesli mesaj söyler
     */
    fun speak(text: String, priority: Int = TextToSpeech.QUEUE_ADD) {
        android.util.Log.d("VoiceGuidance", "speak() called with: $text")
        android.util.Log.d("VoiceGuidance", "isEnabled: $isEnabled, isInitialized: ${_isInitialized.value}")
        
        if (!isEnabled) {
            android.util.Log.d("VoiceGuidance", "Voice guidance disabled")
            return
        }
        
        if (!_isInitialized.value) {
            android.util.Log.d("VoiceGuidance", "TTS not initialized")
            return
        }
        
        textToSpeech?.let { tts ->
            android.util.Log.d("VoiceGuidance", "Speaking: $text")
            val result = tts.speak(text, priority, null, "utterance_$text")
            android.util.Log.d("VoiceGuidance", "TTS speak result: $result")
        }
    }
    
    /**
     * Konuşmayı durdurur
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }
    
    /**
     * Egzersiz başlangıcı için sesli komut
     */
    fun announceExerciseStart(exerciseName: String) {
        speak("Şimdi $exerciseName egzersizine başlıyoruz. Hazır olduğunuzda başlayın.")
    }
    
    /**
     * Egzersiz tamamlanması için sesli komut
     */
    fun announceExerciseComplete(exerciseName: String) {
        speak("Harika! $exerciseName egzersizini başarıyla tamamladınız.")
    }
    
    /**
     * Sonraki egzersize geçiş için sesli komut
     */
    fun announceNextExercise(nextExerciseName: String) {
        speak("Sonraki egzersiz: $nextExerciseName. Hazır olduğunuzda devam edin.")
    }
    
    /**
     * Seans tamamlanması için sesli komut
     */
    fun announceSessionComplete(sessionName: String, totalExercises: Int) {
        speak("Tebrikler! $sessionName seansını başarıyla tamamladınız. " +
              "$totalExercises egzersizi bitirerek 10 puan kazandınız.")
    }
    
    /**
     * Motivasyon mesajları
     */
    fun giveMotivation() {
        val motivationMessages = listOf(
            "Harika gidiyorsunuz! Devam edin!",
            "Mükemmel! Çok iyi çalışıyorsunuz!",
            "Süper! Bu tempoda devam edin!",
            "Bravo! Hedeflerinize yaklaşıyorsunuz!",
            "Muhteşem! Kendinizle gurur duyabilirsiniz!"
        )
        speak(motivationMessages.random())
    }
    
    /**
     * Egzersiz talimatları
     */
    fun giveExerciseInstruction(exerciseName: String) {
        val instructions = getExerciseInstructions(exerciseName)
        if (instructions.isNotEmpty()) {
            speak(instructions)
        }
    }
    
    /**
     * Egzersiz talimatlarını getir
     */
    private fun getExerciseInstructions(exerciseName: String): String {
        return when (exerciseName.lowercase()) {
            "kol kaldırma" -> "Kollarınızı yavaşça yanlara doğru kaldırın. Omuz hizasına kadar çıkarın ve yavaşça indirin."
            "diz bükme" -> "Ayakta dururken, dizinizi yavaşça bükerek topuğunuzu kalçanıza doğru çekin."
            "omuz dönme" -> "Omuzlarınızı yavaş yavaş önden arkaya doğru çevirin. Derin nefes almayı unutmayın."
            "boyun esneme" -> "Başınızı yavaşça sağa sola çevirin. Boyun kaslarınızı gerin ama zorlamayın."
            "bel esneme" -> "Ayakta dururken, ellerinizi bele koyun ve yavaşça geriye doğru eğilin."
            "ayak bileği dönme" -> "Ayak bileğinizi saat yönünde ve ters yönde yavaşça çevirin."
            "sırt germe" -> "Kollarınızı öne uzatın ve sırt kaslarınızı gerin. Derin nefes alın."
            "kalça hareketleri" -> "Kalçanızı yavaşça öne arkaya hareket ettirin. Dengenizi koruyun."
            else -> "Bu egzersizi doğru form ile yapın. Nefes almayı unutmayın."
        }
    }
    
    /**
     * Geri sayım
     */
    fun countdown(seconds: Int) {
        for (i in seconds downTo 1) {
            speak(i.toString(), TextToSpeech.QUEUE_ADD)
        }
        speak("Başlayın!")
    }
    
    /**
     * Ayarları güncelle
     */
    fun updateSettings(enabled: Boolean, rate: Float = speechRate, pitch: Float = speechPitch) {
        isEnabled = enabled
        speechRate = rate
        speechPitch = pitch
        
        textToSpeech?.let { tts ->
            tts.setSpeechRate(speechRate)
            tts.setPitch(speechPitch)
        }
    }
    
    /**
     * Kaynak temizleme
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isInitialized.value = false
    }
}

/**
 * Sesli yönlendirme ayarları
 */
data class VoiceSettings(
    val isEnabled: Boolean = true,
    val speechRate: Float = 0.8f,
    val speechPitch: Float = 1.0f,
    val announceStart: Boolean = true,
    val announceComplete: Boolean = true,
    val giveInstructions: Boolean = true,
    val motivationalMessages: Boolean = true
)
