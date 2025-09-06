package com.example.physiotherapyapp.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiotherapyapp.components.AnimatedProgressBar
import com.example.physiotherapyapp.components.EnhancedCard
import com.example.physiotherapyapp.data.PainEntry
import com.example.physiotherapyapp.data.Session
import com.example.physiotherapyapp.data.User
import com.example.physiotherapyapp.ui.theme.*
import com.example.physiotherapyapp.viewmodel.DailyProgress
import java.text.SimpleDateFormat
import java.util.*

/**
 * Analiz ve raporlar ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    user: User,
    dailyProgress: DailyProgress,
    completedSessions: List<Session>,
    painEntries: List<PainEntry>,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Genel Bakış", "Ağrı Analizi", "Egzersiz İstatistikleri")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analiz & Raporlar",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Debug bilgisi
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔧 DEBUG: Analytics Screen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Completed Sessions: ${completedSessions.size}")
                    Text("Pain Entries: ${painEntries.size}")
                    Text("User Total Sessions: ${user.totalSessions}")
                    Text("User Total Points: ${user.totalPoints}")
                    Text("Daily Progress Sessions: ${dailyProgress.sessionsCompleted}")
                    Text("Daily Progress Points: ${dailyProgress.pointsEarned}")
                }
            }
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            // Tab Content
            when (selectedTab) {
                0 -> OverviewTab(user, dailyProgress, completedSessions)
                1 -> PainAnalysisTab(painEntries, completedSessions)
                2 -> ExerciseStatsTab(completedSessions)
            }
        }
    }
}

/**
 * Genel bakış sekmesi
 */
@Composable
private fun OverviewTab(
    user: User,
    dailyProgress: DailyProgress,
    completedSessions: List<Session>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Günlük hedefler
            DailyGoalsCard(dailyProgress)
        }
        
        item {
            // Haftalık aktivite
            WeeklyActivityCard(completedSessions)
        }
        
        item {
            // Genel istatistikler
            GeneralStatsCard(user, completedSessions)
        }
        
        item {
            // Son aktiviteler
            RecentActivitiesCard(completedSessions.take(5))
        }
    }
}

/**
 * Ağrı analizi sekmesi
 */
@Composable
private fun PainAnalysisTab(
    painEntries: List<PainEntry>,
    completedSessions: List<Session>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Ağrı trend kartı
            PainTrendCard(painEntries)
        }
        
        item {
            // Vücut bölgesi analizi
            BodyPartAnalysisCard(painEntries)
        }
        
        item {
            // Ağrı vs egzersiz korelasyonu
            PainExerciseCorrelationCard(painEntries, completedSessions)
        }
    }
}

/**
 * Egzersiz istatistikleri sekmesi
 */
@Composable
private fun ExerciseStatsTab(
    completedSessions: List<Session>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // En popüler egzersizler
            PopularExercisesCard(completedSessions)
        }
        
        item {
            // Egzersiz sıklığı
            ExerciseFrequencyCard(completedSessions)
        }
        
        item {
            // Süreklilik analizi
            ConsistencyAnalysisCard(completedSessions)
        }
    }
}

/**
 * Günlük hedefler kartı
 */
@Composable
private fun DailyGoalsCard(dailyProgress: DailyProgress) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📈 Günlük Hedefler",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Seans hedefi
            ProgressItem(
                title = "Seans Hedefi",
                current = dailyProgress.sessionsCompleted,
                target = dailyProgress.sessionTarget,
                progress = dailyProgress.sessionProgress,
                icon = "🏃",
                color = HealthyBlue40
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Puan hedefi
            ProgressItem(
                title = "Puan Hedefi",
                current = dailyProgress.pointsEarned,
                target = dailyProgress.pointTarget,
                progress = dailyProgress.pointProgress,
                icon = "⭐",
                color = WarmAccent40
            )
        }
    }
}

/**
 * İlerleme öğesi
 */
@Composable
private fun ProgressItem(
    title: String,
    current: Int,
    target: Int,
    progress: Float,
    icon: String,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$current/$target",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AnimatedProgressBar(
            progress = progress.coerceAtMost(1f),
            progressColor = color,
            backgroundColor = color.copy(alpha = 0.2f)
        )
    }
}

/**
 * Haftalık aktivite kartı
 */
@Composable
private fun WeeklyActivityCard(completedSessions: List<Session>) {
    val weekDays = getLastWeekDays()
    val dailySessionCounts = weekDays.map { day ->
        completedSessions.count { session ->
            isSameDay(session.startDate, day)
        }
    }
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📅 Haftalık Aktivite",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Haftalık chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEachIndexed { index, day ->
                    val count = dailySessionCounts[index]
                    val dayName = SimpleDateFormat("E", Locale("tr")).format(day)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Bar
                        val maxCount = dailySessionCounts.maxOrNull() ?: 1
                        val height = if (count > 0) {
                            ((count.toFloat() / maxCount) * 60 + 20).dp
                        } else 20.dp
                        
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(height)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (count > 0) MedicalGreen40 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (count > 0) MedicalGreen40 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Genel istatistikler kartı
 */
@Composable
private fun GeneralStatsCard(user: User, completedSessions: List<Session>) {
    val averagePointsPerSession = if (user.totalSessions > 0) {
        user.totalPoints / user.totalSessions
    } else 0
    
    val thisWeekSessions = completedSessions.count { session ->
        val weekStart = getWeekStart(Date())
        session.startDate >= weekStart
    }
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📊 Genel İstatistikler",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    title = "Bu Hafta",
                    value = "$thisWeekSessions seans",
                    icon = "📅",
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    title = "Ortalama",
                    value = "$averagePointsPerSession puan/seans",
                    icon = "📈",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    title = "Rozet",
                    value = "${user.badges.size} kazanıldı",
                    icon = "🏆",
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    title = "Seviye",
                    value = "Level ${user.currentLevel}",
                    icon = "⚡",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * İstatistik öğesi
 */
@Composable
private fun StatisticItem(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Son aktiviteler kartı
 */
@Composable
private fun RecentActivitiesCard(recentSessions: List<Session>) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "🕒 Son Aktiviteler",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (recentSessions.isEmpty()) {
                Text(
                    text = "Henüz aktivite yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                recentSessions.forEach { session ->
                    RecentActivityItem(session)
                    if (session != recentSessions.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

/**
 * Son aktivite öğesi
 */
@Composable
private fun RecentActivityItem(session: Session) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MedicalGreen40)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.templateName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${session.exercises.size} egzersiz • ${session.pointsEarned} puan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = SimpleDateFormat("dd/MM", Locale.getDefault()).format(session.startDate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Ağrı trend kartı
 */
@Composable
private fun PainTrendCard(painEntries: List<PainEntry>) {
    val lastWeekEntries = painEntries.filter { entry ->
        val weekAgo = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
        entry.date >= weekAgo
    }
    
    val averagePain = if (lastWeekEntries.isNotEmpty()) {
        lastWeekEntries.map { it.painLevel }.average()
    } else 0.0
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "💚 Ağrı Trend Analizi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", averagePain),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            averagePain <= 3 -> SuccessGreen
                            averagePain <= 6 -> WarmAccent40
                            else -> ErrorRed
                        }
                    )
                    Text(
                        text = "Ortalama Ağrı",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${lastWeekEntries.size}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Bu Hafta Kayıt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Vücut bölgesi analizi kartı
 */
@Composable
private fun BodyPartAnalysisCard(painEntries: List<PainEntry>) {
    val bodyPartCounts = painEntries
        .filter { it.bodyPart.isNotEmpty() }
        .groupingBy { it.bodyPart }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(5)
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🎯 En Çok Ağrı Şikayeti",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (bodyPartCounts.isEmpty()) {
                Text(
                    text = "Henüz vücut bölgesi kaydı yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                bodyPartCounts.forEach { (bodyPart, count) ->
                    BodyPartItem(bodyPart, count, bodyPartCounts.first().second)
                    if (bodyPart != bodyPartCounts.last().first) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Vücut bölgesi öğesi
 */
@Composable
private fun BodyPartItem(bodyPart: String, count: Int, maxCount: Int) {
    val progress = count.toFloat() / maxCount
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = bodyPart,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count kez",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        AnimatedProgressBar(
            progress = progress,
            progressColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * En popüler egzersizler kartı
 */
@Composable
private fun PopularExercisesCard(completedSessions: List<Session>) {
    val exerciseFrequency = completedSessions
        .flatMap { it.exercises }
        .groupingBy { it.name }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(5)
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🏃 En Popüler Egzersizler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (exerciseFrequency.isEmpty()) {
                Text(
                    text = "Henüz egzersiz geçmişi yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                exerciseFrequency.forEach { (exercise, count) ->
                    ExerciseFrequencyItem(exercise, count, exerciseFrequency.first().second)
                    if (exercise != exerciseFrequency.last().first) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Egzersiz sıklığı öğesi
 */
@Composable
private fun ExerciseFrequencyItem(exercise: String, count: Int, maxCount: Int) {
    val progress = count.toFloat() / maxCount
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = exercise,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count kez",
                style = MaterialTheme.typography.bodyMedium,
                color = MedicalGreen40
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        AnimatedProgressBar(
            progress = progress,
            progressColor = MedicalGreen40,
            backgroundColor = MedicalGreen40.copy(alpha = 0.2f)
        )
    }
}

/**
 * Egzersiz sıklığı kartı
 */
@Composable
private fun ExerciseFrequencyCard(completedSessions: List<Session>) {
    val totalExercises = completedSessions.sumOf { it.exercises.size }
    val averageExercisesPerSession = if (completedSessions.isNotEmpty()) {
        totalExercises.toFloat() / completedSessions.size
    } else 0f
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🔢 Egzersiz Sıklığı",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    title = "Toplam",
                    value = "$totalExercises",
                    icon = "💪",
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    title = "Ortalama/Seans",
                    value = String.format("%.1f", averageExercisesPerSession),
                    icon = "📊",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Süreklilik analizi kartı
 */
@Composable
private fun ConsistencyAnalysisCard(completedSessions: List<Session>) {
    val currentStreak = calculateCurrentStreak(completedSessions)
    val bestStreak = calculateBestStreak(completedSessions)
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🔥 Süreklilik Analizi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    title = "Mevcut Seri",
                    value = "$currentStreak gün",
                    icon = "🔥",
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    title = "En İyi Seri",
                    value = "$bestStreak gün",
                    icon = "🏆",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Ağrı-egzersiz korelasyonu kartı
 */
@Composable
private fun PainExerciseCorrelationCard(
    painEntries: List<PainEntry>,
    completedSessions: List<Session>
) {
    // Basit korelasyon analizi
    val sessionsWithPain = completedSessions.mapNotNull { session ->
        val painEntry = painEntries.find { it.sessionId == session.id }
        if (painEntry != null) {
            Pair(session.exercises.size, painEntry.painLevel)
        } else null
    }
    
    val averagePainByExerciseCount = sessionsWithPain
        .groupBy { it.first }
        .mapValues { (_, pairs) ->
            pairs.map { it.second }.average()
        }
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📈 Egzersiz-Ağrı İlişkisi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (sessionsWithPain.isEmpty()) {
                Text(
                    text = "Yeterli veri yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val totalAverage = sessionsWithPain.map { it.second }.average()
                Text(
                    text = "Ortalama ağrı seviyesi: ${String.format("%.1f", totalAverage)}/10",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${sessionsWithPain.size} seans analiz edildi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Yardımcı fonksiyonlar
private fun getLastWeekDays(): List<Date> {
    val days = mutableListOf<Date>()
    val calendar = Calendar.getInstance()
    
    // Son 7 günü al
    for (i in 6 downTo 0) {
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        days.add(calendar.time)
    }
    
    return days
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun getWeekStart(date: Date): Date {
    val cal = Calendar.getInstance().apply { 
        time = date
        firstDayOfWeek = Calendar.MONDAY
    }
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    
    return cal.time
}

private fun calculateCurrentStreak(sessions: List<Session>): Int {
    if (sessions.isEmpty()) return 0
    
    val sortedSessions = sessions.sortedByDescending { it.startDate }
    var streak = 0
    var currentDate = Date()
    
    for (session in sortedSessions) {
        if (isSameDay(session.startDate, currentDate) || 
            isDayBefore(session.startDate, currentDate)) {
            streak++
            currentDate = session.startDate
        } else {
            break
        }
    }
    
    return streak
}

private fun calculateBestStreak(sessions: List<Session>): Int {
    if (sessions.isEmpty()) return 0
    
    val sortedSessions = sessions.sortedBy { it.startDate }
    var bestStreak = 1
    var currentStreak = 1
    
    for (i in 1 until sortedSessions.size) {
        if (isDayAfter(sortedSessions[i].startDate, sortedSessions[i-1].startDate)) {
            currentStreak++
            bestStreak = maxOf(bestStreak, currentStreak)
        } else {
            currentStreak = 1
        }
    }
    
    return bestStreak
}

private fun isDayBefore(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    cal2.add(Calendar.DAY_OF_YEAR, -1)
    
    return isSameDay(cal1.time, cal2.time)
}

private fun isDayAfter(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    cal2.add(Calendar.DAY_OF_YEAR, 1)
    
    return isSameDay(cal1.time, cal2.time)
}
