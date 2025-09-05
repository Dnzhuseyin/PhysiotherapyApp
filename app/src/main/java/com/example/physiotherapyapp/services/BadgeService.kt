package com.example.physiotherapyapp.services

import com.example.physiotherapyapp.data.Badge
import com.example.physiotherapyapp.data.BadgeCategory
import com.example.physiotherapyapp.data.User
import java.util.Date

/**
 * Rozet sistemi servisi
 * Kullanıcının başarılarını takip eder ve rozetler kazandırır
 */
class BadgeService {
    
    /**
     * Firebase entegrasyonu için rozet uygunluğu kontrol eder
     */
    fun checkBadgeEligibility(
        totalSessions: Int,
        totalPoints: Int,
        consecutiveDays: Int,
        currentBadges: List<Badge>
    ): List<Badge> {
        val newBadges = mutableListOf<Badge>()
        val earnedBadgeIds = currentBadges.map { it.id }.toSet()
        
        // Seans sayısı rozetleri
        newBadges.addAll(checkSessionBadges(totalSessions, earnedBadgeIds))
        
        // Süreklilik rozetleri
        newBadges.addAll(checkConsistencyBadges(consecutiveDays, earnedBadgeIds))
        
        // Puan rozetleri
        newBadges.addAll(checkPointBadges(totalPoints, earnedBadgeIds))
        
        return newBadges
    }
    
    /**
     * Kullanıcının yeni kazandığı rozetleri kontrol eder
     */
    fun checkForNewBadges(user: User): List<Badge> {
        val newBadges = mutableListOf<Badge>()
        val earnedBadgeIds = user.badges.map { it.id }.toSet()
        
        // Seans sayısı rozetleri
        newBadges.addAll(checkSessionBadges(user.totalSessions, earnedBadgeIds))
        
        // Süreklilik rozetleri
        newBadges.addAll(checkConsistencyBadges(user.goals.currentDailyStreak, earnedBadgeIds))
        
        // Puan rozetleri
        newBadges.addAll(checkPointBadges(user.totalPoints, earnedBadgeIds))
        
        // Özel rozetler
        newBadges.addAll(checkSpecialBadges(user, earnedBadgeIds))
        
        return newBadges
    }
    
    /**
     * Seans sayısı rozetlerini kontrol eder
     */
    private fun checkSessionBadges(totalSessions: Int, earnedBadgeIds: Set<String>): List<Badge> {
        val badges = mutableListOf<Badge>()
        
        val sessionMilestones = mapOf(
            1 to Pair("first_session", "İlk Adım"),
            5 to Pair("five_sessions", "Başlangıç"),
            10 to Pair("ten_sessions", "Kararlı"),
            25 to Pair("twentyfive_sessions", "Azimli"),
            50 to Pair("fifty_sessions", "Uzman"),
            100 to Pair("hundred_sessions", "Usta"),
            250 to Pair("twofifty_sessions", "Efsane"),
            500 to Pair("fivehundred_sessions", "Kahraman")
        )
        
        sessionMilestones.forEach { (milestone, badgeInfo) ->
            val (badgeId, badgeName) = badgeInfo
            if (totalSessions >= milestone && !earnedBadgeIds.contains(badgeId)) {
                badges.add(
                    Badge(
                        id = badgeId,
                        name = badgeName,
                        description = "$milestone seans tamamladınız!",
                        iconName = "sessions_$milestone",
                        category = BadgeCategory.SESSIONS
                    )
                )
            }
        }
        
        return badges
    }
    
    /**
     * Süreklilik rozetlerini kontrol eder
     */
    private fun checkConsistencyBadges(currentStreak: Int, earnedBadgeIds: Set<String>): List<Badge> {
        val badges = mutableListOf<Badge>()
        
        val streakMilestones = mapOf(
            3 to Pair("streak_3", "3 Gün Üst Üste"),
            7 to Pair("streak_7", "1 Hafta Sürekli"),
            14 to Pair("streak_14", "2 Hafta Disiplinli"),
            30 to Pair("streak_30", "1 Ay Kararlı"),
            60 to Pair("streak_60", "2 Ay Azimli"),
            100 to Pair("streak_100", "100 Gün Kahraman")
        )
        
        streakMilestones.forEach { (milestone, badgeInfo) ->
            val (badgeId, badgeName) = badgeInfo
            if (currentStreak >= milestone && !earnedBadgeIds.contains(badgeId)) {
                badges.add(
                    Badge(
                        id = badgeId,
                        name = badgeName,
                        description = "$milestone gün üst üste egzersiz yaptınız!",
                        iconName = "streak_$milestone",
                        category = BadgeCategory.CONSISTENCY
                    )
                )
            }
        }
        
        return badges
    }
    
    /**
     * Puan rozetlerini kontrol eder
     */
    private fun checkPointBadges(totalPoints: Int, earnedBadgeIds: Set<String>): List<Badge> {
        val badges = mutableListOf<Badge>()
        
        val pointMilestones = mapOf(
            50 to Pair("points_50", "Puanlama Başlangıcı"),
            100 to Pair("points_100", "Yüzlük Kulüp"),
            250 to Pair("points_250", "Puan Avcısı"),
            500 to Pair("points_500", "Puan Ustası"),
            1000 to Pair("points_1000", "Binlik Kulüp"),
            2500 to Pair("points_2500", "Puan Efsanesi")
        )
        
        pointMilestones.forEach { (milestone, badgeInfo) ->
            val (badgeId, badgeName) = badgeInfo
            if (totalPoints >= milestone && !earnedBadgeIds.contains(badgeId)) {
                badges.add(
                    Badge(
                        id = badgeId,
                        name = badgeName,
                        description = "$milestone puan kazandınız!",
                        iconName = "points_$milestone",
                        category = BadgeCategory.POINTS
                    )
                )
            }
        }
        
        return badges
    }
    
    /**
     * Özel rozetleri kontrol eder
     */
    private fun checkSpecialBadges(user: User, earnedBadgeIds: Set<String>): List<Badge> {
        val badges = mutableListOf<Badge>()
        
        // Ağrı takibi rozeti
        if (user.painEntries.size >= 10 && !earnedBadgeIds.contains("pain_tracker")) {
            badges.add(
                Badge(
                    id = "pain_tracker",
                    name = "Ağrı Takipçisi",
                    description = "10 kez ağrı seviyenizi kaydettiniz",
                    iconName = "pain_tracker",
                    category = BadgeCategory.SPECIAL
                )
            )
        }
        
        // Düşük ağrı rozeti
        val lowPainSessions = user.painEntries.filter { it.painLevel <= 3 }.size
        if (lowPainSessions >= 20 && !earnedBadgeIds.contains("low_pain_hero")) {
            badges.add(
                Badge(
                    id = "low_pain_hero",
                    name = "Ağrı Yönetim Uzmanı",
                    description = "20 seansda düşük ağrı seviyesi elde ettiniz",
                    iconName = "low_pain_hero",
                    category = BadgeCategory.SPECIAL
                )
            )
        }
        
        // Çeşitlilik rozeti
        val uniqueExercises = user.completedSessions
            .flatMap { it.exercises }
            .map { it.name }
            .distinct()
            .size
        
        if (uniqueExercises >= 15 && !earnedBadgeIds.contains("exercise_variety")) {
            badges.add(
                Badge(
                    id = "exercise_variety",
                    name = "Çeşitlilik Uzmanı",
                    description = "$uniqueExercises farklı egzersiz türü deneyimlediniz",
                    iconName = "exercise_variety",
                    category = BadgeCategory.SPECIAL
                )
            )
        }
        
        // Hafta sonu savaşçısı
        // Note: Bu özellik için session tarihlerinin hafta sonu olup olmadığını kontrol etmek gerekir
        // Şimdilik basit bir yaklaşım kullanıyoruz
        if (user.totalSessions >= 20 && !earnedBadgeIds.contains("weekend_warrior")) {
            badges.add(
                Badge(
                    id = "weekend_warrior",
                    name = "Hafta Sonu Savaşçısı",
                    description = "Hafta sonlarında da egzersiz yapmayı ihmal etmiyorsunuz",
                    iconName = "weekend_warrior",
                    category = BadgeCategory.SPECIAL
                )
            )
        }
        
        return badges
    }
    
    /**
     * Rozet kategorisine göre renk döndürür
     */
    fun getBadgeColor(category: BadgeCategory): androidx.compose.ui.graphics.Color {
        return when (category) {
            BadgeCategory.SESSIONS -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Mavi
            BadgeCategory.CONSISTENCY -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Yeşil
            BadgeCategory.POINTS -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Turuncu
            BadgeCategory.SPECIAL -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Mor
        }
    }
    
    /**
     * Rozet ikonunu döndürür (basit string temelli)
     */
    fun getBadgeIcon(iconName: String): String {
        return when {
            iconName.contains("session") -> "🏆"
            iconName.contains("streak") -> "🔥"
            iconName.contains("points") -> "⭐"
            iconName.contains("pain") -> "💚"
            iconName.contains("variety") -> "🌈"
            iconName.contains("weekend") -> "⚡"
            else -> "🎖️"
        }
    }
}
