package com.example.physiotherapyapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
 * Ağrı günlüğü geçmişi ekranı
 * Kullanıcı önceki ağrı kayıtlarını görüntüleyebilir, düzenleyebilir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainHistoryScreen(
    painEntries: List<PainEntry>,
    completedSessions: List<Session>,
    onEditPainEntry: (PainEntry, Int, String) -> Unit, // painEntry, newPainLevel, newNotes
    onDeletePainEntry: (PainEntry) -> Unit,
    onBackClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<PainEntry?>(null) }
    var selectedEntry by remember { mutableStateOf<PainEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ağrı Günlüğü Geçmişi",
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
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: Export functionality */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add new manual entry */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Yeni Kayıt"
                )
            }
        }
    ) { paddingValues ->
        if (painEntries.isEmpty()) {
            EmptyPainHistoryContent(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PainHistorySummaryCard(painEntries)
                }
                
                items(painEntries.sortedByDescending { it.date }) { painEntry ->
                    val session = completedSessions.find { it.id == painEntry.sessionId }
                    PainHistoryEntryCard(
                        painEntry = painEntry,
                        session = session,
                        onEdit = { onEditPainEntry(painEntry, painEntry.painLevel, painEntry.notes) },
                        onDelete = { showDeleteDialog = painEntry },
                        onViewDetails = { selectedEntry = painEntry }
                    )
                }
            }
        }
    }
    
    // Silme onay dialog'u
    showDeleteDialog?.let { entry ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Kaydı Sil") },
            text = { 
                Text("Bu ağrı kaydını silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePainEntry(entry)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("İptal")
                }
            }
        )
    }
    
    // Detay dialog'u
    selectedEntry?.let { entry ->
        PainEntryDetailDialog(
            painEntry = entry,
            session = completedSessions.find { it.id == entry.sessionId },
            onDismiss = { selectedEntry = null }
        )
    }
}

/**
 * Ağrı geçmişi özet kartı
 */
@Composable
private fun PainHistorySummaryCard(painEntries: List<PainEntry>) {
    val averagePain = painEntries.map { it.painLevel }.average()
    val lastWeekEntries = painEntries.filter { entry ->
        val weekAgo = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
        entry.date >= weekAgo
    }
    val trend = calculatePainTrend(painEntries)
    
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
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📊 Ağrı Durumu Özeti",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = "Toplam Kayıt",
                    value = "${painEntries.size}",
                    icon = "📝"
                )
                
                SummaryItem(
                    title = "Ortalama Ağrı",
                    value = String.format("%.1f/10", averagePain),
                    icon = "📊"
                )
                
                SummaryItem(
                    title = "Bu Hafta",
                    value = "${lastWeekEntries.size}",
                    icon = "📅"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Trend göstergesi
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(trend.color.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Text(text = trend.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trend.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = trend.color
                )
            }
        }
    }
}

/**
 * Özet öğesi
 */
@Composable
private fun SummaryItem(
    title: String,
    value: String,
    icon: String
) {
    Column(
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
 * Ağrı geçmişi kayıt kartı
 */
@Composable
private fun PainHistoryEntryCard(
    painEntry: PainEntry,
    session: Session?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
) {
    val painColor = getPainLevelColor(painEntry.painLevel)
    
    EnhancedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(painColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = painEntry.painLevel.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
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
                        if (painEntry.bodyPart.isNotEmpty()) {
                            Text(
                                text = "📍 ${painEntry.bodyPart}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Aksiyon butonları
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            if (painEntry.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "💬 ${painEntry.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                )
            }
        }
    }
}

/**
 * Ağrı kaydı detay dialog'u
 */
@Composable
private fun PainEntryDetailDialog(
    painEntry: PainEntry,
    session: Session?,
    onDismiss: () -> Unit
) {
    val painColor = getPainLevelColor(painEntry.painLevel)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ağrı Kaydı Detayları",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Ağrı seviyesi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getPainLevelDescription(painEntry.painLevel),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Seans bilgisi
                DetailRow("🏃 Seans", session?.templateName ?: "Bilinmeyen")
                
                // Tarih
                DetailRow(
                    "📅 Tarih", 
                    SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(painEntry.date)
                )
                
                // Vücut bölgesi
                if (painEntry.bodyPart.isNotEmpty()) {
                    DetailRow("📍 Bölge", painEntry.bodyPart)
                }
                
                // Notlar
                if (painEntry.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💬 Notlar:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = painEntry.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

/**
 * Detay satırı
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * Boş ağrı geçmişi içeriği
 */
@Composable
private fun EmptyPainHistoryContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🩹",
            fontSize = 80.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Henüz Ağrı Kaydı Yok",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Seans tamamladıktan sonra ağrı seviyenizi kaydetmeye başlayın. Bu veriler iyileşme sürecinizi takip etmenize yardımcı olacak.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Yardımcı fonksiyonlar
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
        1 -> "Çok hafif ağrı"
        2, 3 -> "Hafif ağrı"
        4, 5 -> "Orta şiddette ağrı"
        6, 7 -> "Şiddetli ağrı"
        8, 9 -> "Çok şiddetli ağrı"
        10 -> "Dayanılmaz ağrı"
        else -> "Bilinmiyor"
    }
}

private data class PainTrend(
    val icon: String,
    val message: String,
    val color: Color
)

private fun calculatePainTrend(painEntries: List<PainEntry>): PainTrend {
    if (painEntries.size < 4) {
        return PainTrend("❓", "Trend analizi için daha fazla veri gerekli", Color.Gray)
    }
    
    val sortedEntries = painEntries.sortedBy { it.date }
    val firstHalf = sortedEntries.take(sortedEntries.size / 2)
    val secondHalf = sortedEntries.takeLast(sortedEntries.size / 2)
    
    val firstAverage = firstHalf.map { it.painLevel }.average()
    val secondAverage = secondHalf.map { it.painLevel }.average()
    
    val difference = firstAverage - secondAverage
    
    return when {
        difference > 1.0 -> PainTrend(
            "📉", 
            "Ağrı seviyenizde belirgin iyileşme var!", 
            Color(0xFF4CAF50)
        )
        difference > 0.3 -> PainTrend(
            "📈", 
            "Ağrı seviyenizde hafif iyileşme var", 
            Color(0xFF8BC34A)
        )
        difference < -1.0 -> PainTrend(
            "📈", 
            "Ağrı seviyenizde artış var, doktorunuza danışın", 
            Color(0xFFF44336)
        )
        difference < -0.3 -> PainTrend(
            "⚠️", 
            "Ağrı seviyenizde hafif artış var", 
            Color(0xFFFF9800)
        )
        else -> PainTrend(
            "➡️", 
            "Ağrı seviyeniz stabil seyrediyor", 
            Color.Gray
        )
    }
}
