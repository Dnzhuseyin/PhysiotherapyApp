package com.example.physiotherapyapp.services

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Google AI (Gemini) destekli öneri servisi
 * Kullanıcı profiline göre özelleştirilmiş egzersiz programları önerir
 */
class AIRecommendationService {
    
    private val apiKey = "AIzaSyAbwaIqvsufOwIgfiLwk-M5DcrHXCg26ww"
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey
    )
    
    /**
     * Kullanıcı profili için AI destekli seans önerisi
     */
    suspend fun generateSessionRecommendation(
        userProfile: UserProfile,
        currentPainLevel: Int? = null,
        previousSessions: List<String> = emptyList()
    ): AISessionRecommendation = withContext(Dispatchers.IO) {
        
        val prompt = buildSessionPrompt(userProfile, currentPainLevel, previousSessions)
        
        try {
            val response = generativeModel.generateContent(prompt)
            parseSessionRecommendation(response, userProfile)
        } catch (e: Exception) {
            // Fallback önerileri
            getFallbackRecommendation(userProfile)
        }
    }
    
    /**
     * Kullanıcının egzersiz geçmişine dayalı iyileştirme önerileri
     */
    suspend fun generateImprovementSuggestions(
        userProfile: UserProfile,
        painHistory: List<Int>,
        completedExercises: List<String>
    ): List<String> = withContext(Dispatchers.IO) {
        
        val prompt = buildImprovementPrompt(userProfile, painHistory, completedExercises)
        
        try {
            val response = generativeModel.generateContent(prompt)
            parseImprovementSuggestions(response.text ?: "")
        } catch (e: Exception) {
            getFallbackImprovements(userProfile)
        }
    }
    
    /**
     * Kullanıcı profiline özel egzersiz açıklamaları
     */
    suspend fun getPersonalizedExerciseDescription(
        exerciseName: String,
        userProfile: UserProfile,
        currentCondition: String? = null
    ): String = withContext(Dispatchers.IO) {
        
        val prompt = buildExercisePrompt(exerciseName, userProfile, currentCondition)
        
        try {
            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: getDefaultExerciseDescription(exerciseName)
        } catch (e: Exception) {
            getDefaultExerciseDescription(exerciseName)
        }
    }
    
    /**
     * Seans önerisi prompt'u oluşturur
     */
    private fun buildSessionPrompt(
        userProfile: UserProfile,
        currentPainLevel: Int?,
        previousSessions: List<String>
    ): String {
        return """
            Sen bir uzman fizyoterapistsin. Aşağıdaki kullanıcı profili için özelleştirilmiş egzersiz seansı öner:
            
            Kullanıcı Profili:
            - Kategori: ${userProfile.category.displayName}
            - Yaş Grubu: ${userProfile.ageGroup.displayName}
            - Ana Şikayet: ${userProfile.primaryComplaint}
            - Hedef: ${userProfile.goal}
            - Aktivite Seviyesi: ${userProfile.activityLevel.displayName}
            - Kısıtlamalar: ${userProfile.limitations.joinToString(", ")}
            
            ${currentPainLevel?.let { "Mevcut Ağrı Seviyesi: $it/10" } ?: ""}
            ${if (previousSessions.isNotEmpty()) "Son Yapılan Seanslar: ${previousSessions.joinToString(", ")}" else ""}
            
            Lütfen aşağıdaki formatla cevap ver:
            SEANS_ADI: [Seans için uygun bir isim]
            AÇIKLAMA: [Seans hakkında kısa açıklama]
            SÜRESİ: [Tahmini süre dakika cinsinden]
            EGZERSİZLER: [Egzersiz1|Egzersiz2|Egzersiz3|Egzersiz4|Egzersiz5]
            DİKKAT: [Özel dikkat edilmesi gereken noktalar]
            
            Egzersiz isimleri şu listeden seçilmeli:
            Kol Kaldırma, Diz Bükme, Omuz Dönme, Boyun Esneme, Bel Esneme, Ayak Bileği Dönme, Sırt Germe, Kalça Hareketleri
        """.trimIndent()
    }
    
    /**
     * İyileştirme önerileri prompt'u
     */
    private fun buildImprovementPrompt(
        userProfile: UserProfile,
        painHistory: List<Int>,
        completedExercises: List<String>
    ): String {
        return """
            Fizyoterapist olarak, kullanıcının ağrı geçmişi ve egzersiz verilerini analiz et:
            
            Kullanıcı: ${userProfile.category.displayName}
            Ağrı Seviyeleri (son 10 kayıt): ${painHistory.takeLast(10).joinToString(", ")}
            En Çok Yapılan Egzersizler: ${completedExercises.joinToString(", ")}
            
            Lütfen şu formatta 3-5 adet kısa iyileştirme önerisi ver:
            ÖNERİ: [Somut ve uygulanabilir öneri]
            
            Öneriler şunlar hakkında olabilir:
            - Egzersiz sıklığı/yoğunluğu
            - Yaşam tarzı değişiklikleri
            - Dikkat edilmesi gerekenler
            - Motivasyon artırıcı tavsiyeler
        """.trimIndent()
    }
    
    /**
     * Egzersiz açıklaması prompt'u
     */
    private fun buildExercisePrompt(
        exerciseName: String,
        userProfile: UserProfile,
        currentCondition: String?
    ): String {
        return """
            "$exerciseName" egzersizi için ${userProfile.category.displayName} kategorisindeki bir kullanıcıya uygun açıklama yaz.
            
            Kullanıcı bilgileri:
            - Kategori: ${userProfile.category.displayName}
            - Yaş: ${userProfile.ageGroup.displayName}
            - Kısıtlamalar: ${userProfile.limitations.joinToString(", ")}
            ${currentCondition?.let { "- Mevcut durum: $it" } ?: ""}
            
            Açıklama şu şekilde olmalı:
            - Maksimum 2-3 cümle
            - Nasıl yapılacağı net olmalı
            - Bu kullanıcı grubuna özel dikkat noktaları
            - Pozitif ve motive edici ton
            
            Sadece açıklama metnini ver, başlık veya ekstra bilgi ekleme.
        """.trimIndent()
    }
    
    /**
     * AI yanıtını seans önerisine çevirir
     */
    private fun parseSessionRecommendation(
        response: GenerateContentResponse,
        userProfile: UserProfile
    ): AISessionRecommendation {
        val text = response.text ?: ""
        
        try {
            val lines = text.lines()
            var sessionName = "Özel Seans"
            var description = "Kişiselleştirilmiş egzersiz seansı"
            var duration = "25"
            var exercises = listOf<String>()
            var notes = ""
            
            lines.forEach { line ->
                when {
                    line.startsWith("SEANS_ADI:") -> {
                        sessionName = line.substringAfter(":").trim()
                    }
                    line.startsWith("AÇIKLAMA:") -> {
                        description = line.substringAfter(":").trim()
                    }
                    line.startsWith("SÜRESİ:") -> {
                        duration = line.substringAfter(":").trim().replace("\\D".toRegex(), "")
                    }
                    line.startsWith("EGZERSİZLER:") -> {
                        exercises = line.substringAfter(":").trim()
                            .split("|")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                    }
                    line.startsWith("DİKKAT:") -> {
                        notes = line.substringAfter(":").trim()
                    }
                }
            }
            
            return AISessionRecommendation(
                sessionName = sessionName,
                description = description,
                exercises = exercises,
                estimatedDuration = "${duration} dk",
                specialNotes = notes,
                confidence = 0.9f
            )
        } catch (e: Exception) {
            return getFallbackRecommendation(userProfile)
        }
    }
    
    /**
     * İyileştirme önerilerini parse eder
     */
    private fun parseImprovementSuggestions(text: String): List<String> {
        return text.lines()
            .filter { it.startsWith("ÖNERİ:") }
            .map { it.substringAfter(":").trim() }
            .filter { it.isNotEmpty() }
            .take(5)
    }
    
    /**
     * Fallback seans önerisi
     */
    private fun getFallbackRecommendation(userProfile: UserProfile): AISessionRecommendation {
        return when (userProfile.category) {
            UserCategory.ATHLETE -> AISessionRecommendation(
                sessionName = "Sporcu Performans Seansı",
                description = "Atletik performansı artırmaya yönelik egzersizler",
                exercises = listOf("Kol Kaldırma", "Diz Bükme", "Omuz Dönme", "Kalça Hareketleri", "Sırt Germe"),
                estimatedDuration = "30 dk",
                specialNotes = "Yüksek tempoda yapın, dinlenme süreleri kısa tutun",
                confidence = 0.7f
            )
            UserCategory.POST_SURGERY -> AISessionRecommendation(
                sessionName = "Ameliyat Sonrası İyileşme",
                description = "Yumuşak iyileşme egzersizleri",
                exercises = listOf("Boyun Esneme", "Ayak Bileği Dönme", "Bel Esneme"),
                estimatedDuration = "15 dk",
                specialNotes = "Yavaş ve kontrollü hareketler yapın, ağrı hissetmemeye dikkat edin",
                confidence = 0.7f
            )
            UserCategory.ELDERLY -> AISessionRecommendation(
                sessionName = "Yaşlılar İçin Gentle Egzersiz",
                description = "Yaşlılar için güvenli ve etkili hareketler",
                exercises = listOf("Boyun Esneme", "Omuz Dönme", "Ayak Bileği Dönme", "Bel Esneme"),
                estimatedDuration = "20 dk",
                specialNotes = "Dengeyi koruyun, yavaş ve dikkatli hareket edin",
                confidence = 0.7f
            )
            UserCategory.GENERAL -> AISessionRecommendation(
                sessionName = "Genel Sağlık Seansı",
                description = "Genel sağlık için dengeli egzersiz programı",
                exercises = listOf("Kol Kaldırma", "Diz Bükme", "Omuz Dönme", "Sırt Germe"),
                estimatedDuration = "25 dk",
                specialNotes = "Vücut dinleme becerinizi kullanın, rahatsızlık hissetmemeye dikkat edin",
                confidence = 0.7f
            )
        }
    }
    
    /**
     * Fallback iyileştirme önerileri
     */
    private fun getFallbackImprovements(userProfile: UserProfile): List<String> {
        return when (userProfile.category) {
            UserCategory.ATHLETE -> listOf(
                "Antrenman öncesi ısınma sürenizi artırın",
                "İyileşme günlerine önem verin",
                "Hidratasyonunuzu artırın"
            )
            UserCategory.POST_SURGERY -> listOf(
                "Doktor kontrollerinizi aksatmayın",
                "Egzersizleri yavaş ve kontrollü yapın",
                "Ağrı hissettiğinizde duraklatın"
            )
            UserCategory.ELDERLY -> listOf(
                "Günlük kısa yürüyüşler yapın",
                "Denge egzersizlerine odaklanın",
                "Sosyal aktivitelere katılın"
            )
            UserCategory.GENERAL -> listOf(
                "Düzenli egzersiz alışkanlığı edinin",
                "Stres yönetimi teknikleri öğrenin",
                "Yeterli uyku almaya özen gösterin"
            )
        }
    }
    
    /**
     * Varsayılan egzersiz açıklaması
     */
    private fun getDefaultExerciseDescription(exerciseName: String): String {
        return when (exerciseName) {
            "Kol Kaldırma" -> "Kollarınızı yavaşça yanlara doğru kaldırın, omuz hizasına kadar çıkartın ve kontrollü şekilde indirin."
            "Diz Bükme" -> "Ayakta dururken dizinizi arkaya doğru bükerek topuğunuzu kalçanıza yaklaştırın."
            "Omuz Dönme" -> "Omuzlarınızı yavaş yavaş ileri ve arkaya doğru döndürün."
            "Boyun Esneme" -> "Başınızı yavaşça sağa sola çevirin, boyun kaslarını gerin."
            "Bel Esneme" -> "Ellerinizi belinize koyarak yavaşça geriye doğru eğilin."
            "Ayak Bileği Dönme" -> "Ayak bileğinizi saat yönünde ve ters yönde çevirin."
            "Sırt Germe" -> "Kollarınızı öne uzatarak sırt kaslarınızı gerin."
            "Kalça Hareketleri" -> "Kalçanızı yavaşça öne arkaya hareket ettirin."
            else -> "Bu egzersizi doğru form ile yapın ve nefes almayı unutmayın."
        }
    }
}

/**
 * AI seans önerisi data class'ı
 */
data class AISessionRecommendation(
    val sessionName: String,
    val description: String,
    val exercises: List<String>,
    val estimatedDuration: String,
    val specialNotes: String,
    val confidence: Float // 0.0 - 1.0 arası AI güven oranı
)

/**
 * Kullanıcı kategorileri
 */
enum class UserCategory(val displayName: String) {
    ATHLETE("Sporcu"),
    POST_SURGERY("Ameliyat Sonrası"),
    ELDERLY("Yaşlı Bireyler"),
    GENERAL("Genel Kullanıcı")
}

/**
 * Yaş grupları
 */
enum class AgeGroup(val displayName: String, val range: String) {
    YOUNG("Genç", "18-30"),
    MIDDLE("Orta Yaş", "31-50"),
    MATURE("Olgun", "51-65"),
    SENIOR("Yaşlı", "65+")
}

/**
 * Aktivite seviyeleri
 */
enum class ActivityLevel(val displayName: String) {
    SEDENTARY("Sedanter"),
    LIGHT("Hafif Aktif"),
    MODERATE("Orta Aktif"),
    HIGH("Çok Aktif"),
    ATHLETE("Atletik")
}

/**
 * Kullanıcı profili
 */
data class UserProfile(
    val category: UserCategory,
    val ageGroup: AgeGroup,
    val activityLevel: ActivityLevel,
    val primaryComplaint: String, // Ana şikayet
    val goal: String, // Hedef
    val limitations: List<String> = emptyList(), // Kısıtlamalar
    val medicalHistory: List<String> = emptyList() // Tıbbi geçmiş
)
