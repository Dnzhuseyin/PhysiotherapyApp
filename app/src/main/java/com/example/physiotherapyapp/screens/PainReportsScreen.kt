package com.example.physiotherapyapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiotherapyapp.components.EnhancedCard
import com.example.physiotherapyapp.data.PainEntry
import com.example.physiotherapyapp.data.Session
import com.example.physiotherapyapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Ağrı raporları ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainReportsScreen(
    painEntries: List<PainEntry>,
    completedSessions: List<Session>,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ağrı Kayıtları", "Trend Analizi", "Rapor Özeti")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ağrı Günlüğü Raporları",
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
                0 -> PainEntriesTab(painEntries, completedSessions)
                1 -> TrendAnalysisTab(painEntries)
                2 -> ReportSummaryTab(painEntries, completedSessions)
            }
        }
    }
}

/**
 * Ağrı kayıtları sekmesi
 */
@Composable
private fun PainEntriesTab(
    painEntries: List<PainEntry>,
    completedSessions: List<Session>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Özet kartı
            PainEntriesSummaryCard(painEntries)
        }
        
        if (painEntries.isEmpty()) {
            item {
                EmptyPainEntriesCard()
            }
        } else {
            items(painEntries.sortedByDescending { it.date }) { painEntry ->
                val session = completedSessions.find { it.id == painEntry.sessionId }
                PainEntryCard(painEntry, session)
            }
        }
    }
}

/**
 * Trend analizi sekmesi
 */
@Composable
private fun TrendAnalysisTab(painEntries: List<PainEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Haftalık trend
            WeeklyPainTrendCard(painEntries)
        }
        
        item {
            // Ağrı seviyesi dağılımı
            PainLevelDistributionCard(painEntries)
        }
        
        item {
            // Vücut bölgesi analizi
            BodyPartPainAnalysisCard(painEntries)
        }
    }
}

/**
 * Rapor özeti sekmesi
 */
@Composable
private fun ReportSummaryTab(
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
            // Genel özet
            GeneralPainSummaryCard(painEntries, completedSessions)
        }
        
        item {
            // İyileşme analizi
            ImprovementAnalysisCard(painEntries)
        }
        
        item {
            // Öneriler
            RecommendationsCard(painEntries)
        }
    }
}

/**
 * Ağrı kayıtları özet kartı
 */
@Composable
private fun PainEntriesSummaryCard(painEntries: List<PainEntry>) {
    val averagePain = if (painEntries.isNotEmpty()) {
        painEntries.map { it.painLevel }.average()
    } else 0.0
    
    val lastWeekEntries = painEntries.filter { entry ->
        val weekAgo = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
        entry.date >= weekAgo
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
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📋 Ağrı Kayıtları Özeti",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStatItem(
                    title = "Toplam Kayıt",
                    value = "${painEntries.size}",
                    icon = "📝",
                    modifier = Modifier.weight(1f)
                )
                
                SummaryStatItem(
                    title = "Ortalama Ağrı",
                    value = String.format("%.1f/10", averagePain),
                    icon = "📊",
                    modifier = Modifier.weight(1f)
                )
                
                SummaryStatItem(
                    title = "Bu Hafta",
                    value = "${lastWeekEntries.size}",
                    icon = "📅",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Özet istatistik öğesi
 */
@Composable
private fun SummaryStatItem(
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
            style = MaterialTheme.typography.headlineSmall,
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
 * Boş ağrı kayıtları kartı
 */
@Composable
private fun EmptyPainEntriesCard() {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🩹",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Henüz Ağrı Kaydı Yok",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Seans tamamladıktan sonra ağrı seviyenizi kaydetmeye başlayın",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Ağrı kaydı kartı
 */
@Composable
private fun PainEntryCard(
    painEntry: PainEntry,
    session: Session?
) {
    val painColor = getPainLevelColor(painEntry.painLevel)
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ağrı seviyesi göstergesi
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(painColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = painEntry.painLevel.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = session?.templateName ?: "Bilinmeyen Seans",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(painEntry.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = getPainLevelDescription(painEntry.painLevel),
                    style = MaterialTheme.typography.bodySmall,
                    color = painColor,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (painEntry.bodyPart.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = painEntry.bodyPart,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (painEntry.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = painEntry.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Haftalık ağrı trend kartı
 */
@Composable
private fun WeeklyPainTrendCard(painEntries: List<PainEntry>) {
    val lastWeeks = getLastFourWeeks()
    val weeklyAverages = lastWeeks.map { week ->
        val weekEntries = painEntries.filter { entry ->
            entry.date >= week.first && entry.date <= week.second
        }
        if (weekEntries.isNotEmpty()) {
            weekEntries.map { it.painLevel }.average()
        } else 0.0
    }
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📈 Son 4 Hafta Ağrı Trendi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyAverages.forEachIndexed { index, average ->
                    val weekLabel = "Hafta ${index + 1}"
                    WeeklyTrendItem(weekLabel, average)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Trend açıklaması
            val trend = calculateTrend(weeklyAverages)
            TrendIndicator(trend)
        }
    }
}

/**
 * Haftalık trend öğesi
 */
@Composable
private fun WeeklyTrendItem(weekLabel: String, average: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bar
        val height = if (average > 0) ((average / 10.0) * 60 + 20).dp else 20.dp
        val color = getPainLevelColor(average.toInt())
        
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(height)
                .clip(RoundedCornerShape(4.dp))
                .background(if (average > 0) color else MaterialTheme.colorScheme.surfaceVariant)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = weekLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = if (average > 0) String.format("%.1f", average) else "-",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (average > 0) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Trend göstergesi
 */
@Composable
private fun TrendIndicator(trend: TrendDirection) {
    val (icon, text, color) = when (trend) {
        TrendDirection.IMPROVING -> Triple("📉", "Ağrı seviyeniz azalıyor", SuccessGreen)
        TrendDirection.WORSENING -> Triple("📈", "Ağrı seviyeniz artıyor", ErrorRed)
        TrendDirection.STABLE -> Triple("➡️", "Ağrı seviyeniz stabil", MaterialTheme.colorScheme.onSurface)
        TrendDirection.INSUFFICIENT_DATA -> Triple("❓", "Yeterli veri yok", MaterialTheme.colorScheme.onSurfaceVariant)
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * Ağrı seviyesi dağılımı kartı
 */
@Composable
private fun PainLevelDistributionCard(painEntries: List<PainEntry>) {
    val distribution = (0..10).map { level ->
        level to painEntries.count { it.painLevel == level }
    }.filter { it.second > 0 }
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📊 Ağrı Seviyesi Dağılımı",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (distribution.isEmpty()) {
                Text(
                    text = "Henüz veri yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                distribution.forEach { (level, count) ->
                    PainLevelDistributionItem(level, count, painEntries.size)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Ağrı seviyesi dağılım öğesi
 */
@Composable
private fun PainLevelDistributionItem(level: Int, count: Int, total: Int) {
    val percentage = (count.toFloat() / total * 100).toInt()
    val progress = count.toFloat() / total
    val color = getPainLevelColor(level)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = level.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = getPainLevelDescription(level),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$count kez (%$percentage)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

/**
 * Vücut bölgesi ağrı analizi kartı
 */
@Composable
private fun BodyPartPainAnalysisCard(painEntries: List<PainEntry>) {
    val bodyPartStats = painEntries
        .filter { it.bodyPart.isNotEmpty() }
        .groupBy { it.bodyPart }
        .mapValues { (_, entries) ->
            BodyPartStats(
                count = entries.size,
                averagePain = entries.map { it.painLevel }.average()
            )
        }
        .toList()
        .sortedByDescending { it.second.count }
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🎯 Vücut Bölgesi Analizi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (bodyPartStats.isEmpty()) {
                Text(
                    text = "Vücut bölgesi kaydı yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                bodyPartStats.forEach { (bodyPart, stats) ->
                    BodyPartStatsItem(bodyPart, stats)
                    if (bodyPart != bodyPartStats.last().first) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

/**
 * Vücut bölgesi istatistik öğesi
 */
@Composable
private fun BodyPartStatsItem(bodyPart: String, stats: BodyPartStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bodyPart,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${stats.count} kayıt • Ortalama: ${String.format("%.1f", stats.averagePain)}/10",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(getPainLevelColor(stats.averagePain.toInt())),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%.0f", stats.averagePain),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Genel ağrı özeti kartı
 */
@Composable
private fun GeneralPainSummaryCard(
    painEntries: List<PainEntry>,
    completedSessions: List<Session>
) {
    val sessionsWithPain = completedSessions.count { session ->
        painEntries.any { it.sessionId == session.id }
    }
    
    val painComplianceRate = if (completedSessions.isNotEmpty()) {
        (sessionsWithPain.toFloat() / completedSessions.size * 100).toInt()
    } else 0
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📋 Genel Özet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStatItem(
                    title = "Kayıt Oranı",
                    value = "%$painComplianceRate",
                    icon = "📝",
                    modifier = Modifier.weight(1f)
                )
                
                SummaryStatItem(
                    title = "Kayıtlı Seans",
                    value = "$sessionsWithPain/${completedSessions.size}",
                    icon = "🏃",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * İyileşme analizi kartı
 */
@Composable
private fun ImprovementAnalysisCard(painEntries: List<PainEntry>) {
    val sortedEntries = painEntries.sortedBy { it.date }
    val improvement = if (sortedEntries.size >= 2) {
        val firstHalf = sortedEntries.take(sortedEntries.size / 2)
        val secondHalf = sortedEntries.takeLast(sortedEntries.size / 2)
        
        val firstAverage = firstHalf.map { it.painLevel }.average()
        val secondAverage = secondHalf.map { it.painLevel }.average()
        
        firstAverage - secondAverage
    } else 0.0
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📈 İyileşme Analizi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                improvement > 1.0 -> {
                    ImprovementIndicator("✅", "Belirgin İyileşme", SuccessGreen)
                    Text(
                        text = "Ağrı seviyenizde ${String.format("%.1f", improvement)} puanlık iyileşme var!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                improvement > 0.5 -> {
                    ImprovementIndicator("📈", "Hafif İyileşme", WarmAccent40)
                    Text(
                        text = "Ağrı seviyenizde hafif bir iyileşme gözlemleniyor.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                improvement < -0.5 -> {
                    ImprovementIndicator("⚠️", "Kötüleşme", ErrorRed)
                    Text(
                        text = "Ağrı seviyenizde artış var. Doktorunuzla görüşmeyi düşünün.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    ImprovementIndicator("➡️", "Stabil", MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "Ağrı seviyeniz stabil seyrediyor.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * İyileşme göstergesi
 */
@Composable
private fun ImprovementIndicator(icon: String, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * Öneriler kartı
 */
@Composable
private fun RecommendationsCard(painEntries: List<PainEntry>) {
    val averagePain = if (painEntries.isNotEmpty()) {
        painEntries.map { it.painLevel }.average()
    } else 0.0
    
    val recommendations = getRecommendations(averagePain, painEntries.size)
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "💡 Öneriler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            recommendations.forEach { recommendation ->
                RecommendationItem(recommendation)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Öneri öğesi
 */
@Composable
private fun RecommendationItem(recommendation: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = recommendation,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

// Yardımcı fonksiyonlar ve data class'lar
private fun getPainLevelColor(level: Int): Color {
    return when (level) {
        0, 1 -> Color(0xFF4CAF50) // Yeşil
        2, 3 -> Color(0xFF8BC34A) // Açık yeşil
        4, 5 -> Color(0xFFFF9800) // Turuncu
        6, 7 -> Color(0xFFFF5722) // Koyu turuncu
        8, 9, 10 -> Color(0xFFF44336) // Kırmızı
        else -> Color.Gray
    }
}

private fun getPainLevelDescription(level: Int): String {
    return when (level) {
        0 -> "Ağrı yok"
        1 -> "Çok hafif"
        2, 3 -> "Hafif"
        4, 5 -> "Orta"
        6, 7 -> "Şiddetli"
        8, 9 -> "Çok şiddetli"
        10 -> "Dayanılmaz"
        else -> "Bilinmiyor"
    }
}

private fun getLastFourWeeks(): List<Pair<Date, Date>> {
    val weeks = mutableListOf<Pair<Date, Date>>()
    val calendar = Calendar.getInstance()
    
    for (i in 3 downTo 0) {
        calendar.time = Date()
        calendar.add(Calendar.WEEK_OF_YEAR, -i)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = calendar.time
        
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val weekEnd = calendar.time
        
        weeks.add(Pair(weekStart, weekEnd))
    }
    
    return weeks
}

private fun calculateTrend(weeklyAverages: List<Double>): TrendDirection {
    val validAverages = weeklyAverages.filter { it > 0 }
    if (validAverages.size < 2) return TrendDirection.INSUFFICIENT_DATA
    
    val firstHalf = validAverages.take(validAverages.size / 2).average()
    val secondHalf = validAverages.takeLast(validAverages.size / 2).average()
    
    return when {
        secondHalf < firstHalf - 0.5 -> TrendDirection.IMPROVING
        secondHalf > firstHalf + 0.5 -> TrendDirection.WORSENING
        else -> TrendDirection.STABLE
    }
}

private fun getRecommendations(averagePain: Double, entryCount: Int): List<String> {
    val recommendations = mutableListOf<String>()
    
    if (entryCount < 5) {
        recommendations.add("Düzenli olarak ağrı seviyenizi kaydetmeye devam edin")
    }
    
    when {
        averagePain <= 3 -> {
            recommendations.add("Ağrı seviyeniz düşük, mevcut egzersiz programınıza devam edin")
            recommendations.add("Düzenli egzersiz yapmaya devam ederek bu seviyeyi koruyun")
        }
        averagePain <= 6 -> {
            recommendations.add("Orta seviye ağrı yaşıyorsunuz, egzersiz yoğunluğunu gözden geçirin")
            recommendations.add("Doktorunuzla mevcut tedavi planınızı değerlendirin")
        }
        else -> {
            recommendations.add("Yüksek ağrı seviyesi - doktorunuzla acil görüşme planlayın")
            recommendations.add("Egzersiz öncesi ısınma ve sonrası soğuma hareketlerine özen gösterin")
            recommendations.add("Ağrı kesici kullanımı hakkında doktorunuza danışın")
        }
    }
    
    return recommendations
}

private enum class TrendDirection {
    IMPROVING, WORSENING, STABLE, INSUFFICIENT_DATA
}

private data class BodyPartStats(
    val count: Int,
    val averagePain: Double
)
