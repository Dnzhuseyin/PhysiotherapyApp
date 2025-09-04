package com.example.physiotherapyapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiotherapyapp.components.EnhancedCard
import com.example.physiotherapyapp.components.GradientButton
import com.example.physiotherapyapp.data.PainEntry
import com.example.physiotherapyapp.ui.theme.*

/**
 * Ağrı günlüğü ekranı - Seans sonrası ağrı seviyesi kaydı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainDiaryScreen(
    sessionId: String,
    sessionName: String,
    onPainSubmitted: (PainEntry) -> Unit,
    onSkip: () -> Unit
) {
    var selectedPainLevel by remember { mutableIntStateOf(-1) }
    var selectedBodyPart by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val painColors = listOf(
        SuccessGreen, // 0-1: Çok az
        Color(0xFF4CAF50), // 2-3: Az
        Color(0xFF8BC34A), // 4-5: Orta
        WarningOrange, // 6-7: Çok
        Color(0xFFFF5722), // 8-9: Şiddetli
        ErrorRed // 10: Dayanılmaz
    )
    
    val bodyParts = listOf(
        "Boyun", "Omuz", "Kol", "Dirsek", "Bilek", 
        "Sırt", "Bel", "Kalça", "Diz", "Ayak Bileği", "Genel"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ağrı Günlüğü",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = sessionName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("Atla")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Ağrı seviyesi seçimi
            PainLevelSelection(
                selectedLevel = selectedPainLevel,
                onLevelSelected = { selectedPainLevel = it },
                painColors = painColors
            )
            
            // Vücut bölgesi seçimi
            BodyPartSelection(
                selectedBodyPart = selectedBodyPart,
                onBodyPartSelected = { selectedBodyPart = it },
                bodyParts = bodyParts
            )
            
            // Notlar
            NotesSection(
                notes = notes,
                onNotesChanged = { notes = it }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Kaydet butonu
            GradientButton(
                text = "Ağrı Kaydını Tamamla",
                onClick = {
                    if (selectedPainLevel >= 0) {
                        val painEntry = PainEntry(
                            sessionId = sessionId,
                            painLevel = selectedPainLevel,
                            bodyPart = selectedBodyPart,
                            notes = notes
                        )
                        onPainSubmitted(painEntry)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Save,
                colors = listOf(HealthyBlue40, MedicalGreen40)
            )
        }
    }
}

/**
 * Ağrı seviyesi seçimi kartı
 */
@Composable
private fun PainLevelSelection(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit,
    painColors: List<Color>
) {
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
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ağrı Seviyeniz (0-10)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (selectedLevel >= 0) "Seçilen: ${getPainDescription(selectedLevel)}" 
                      else "Lütfen ağrı seviyenizi seçin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ağrı seviyesi grid'i
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(11) { level ->
                    PainLevelButton(
                        level = level,
                        isSelected = selectedLevel == level,
                        color = getPainColor(level, painColors),
                        onClick = { onLevelSelected(level) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Açıklama satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hiç Ağrı Yok",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Dayanılmaz Ağrı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Ağrı seviyesi butonu
 */
@Composable
private fun PainLevelButton(
    level: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) color else color.copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.toString(),
            color = if (isSelected) Color.White else color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isSelected) 18.sp else 16.sp
        )
    }
}

/**
 * Vücut bölgesi seçimi
 */
@Composable
private fun BodyPartSelection(
    selectedBodyPart: String,
    onBodyPartSelected: (String) -> Unit,
    bodyParts: List<String>
) {
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
                    imageVector = Icons.Default.Accessibility,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ağrı Bölgesi (İsteğe Bağlı)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(160.dp)
            ) {
                items(bodyParts) { bodyPart ->
                    BodyPartChip(
                        text = bodyPart,
                        isSelected = selectedBodyPart == bodyPart,
                        onClick = { 
                            onBodyPartSelected(
                                if (selectedBodyPart == bodyPart) "" else bodyPart
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Vücut bölgesi chip'i
 */
@Composable
private fun BodyPartChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            textAlign = TextAlign.Center,
            color = if (isSelected) Color.White 
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

/**
 * Notlar bölümü
 */
@Composable
private fun NotesSection(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
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
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ek Notlar (İsteğe Bağlı)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                label = { Text("Ağrınız hakkında detaylar...") },
                placeholder = { Text("Örnek: Egzersiz sırasında hafif sıkışma hissettim") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}

/**
 * Ağrı seviyesi için renk döndürür
 */
private fun getPainColor(level: Int, painColors: List<Color>): Color {
    return when (level) {
        0, 1 -> painColors[0]
        2, 3 -> painColors[1]
        4, 5 -> painColors[2]
        6, 7 -> painColors[3]
        8, 9 -> painColors[4]
        10 -> painColors[5]
        else -> painColors[0]
    }
}

/**
 * Ağrı seviyesi açıklaması
 */
private fun getPainDescription(level: Int): String {
    return when (level) {
        0 -> "Hiç ağrı yok"
        1 -> "Çok hafif ağrı"
        2 -> "Hafif ağrı"
        3 -> "Hafif-orta ağrı"
        4 -> "Orta ağrı"
        5 -> "Orta-şiddetli ağrı"
        6 -> "Şiddetli ağrı"
        7 -> "Çok şiddetli ağrı"
        8 -> "Şiddetli ağrı"
        9 -> "Aşırı şiddetli ağrı"
        10 -> "Dayanılmaz ağrı"
        else -> "Bilinmiyor"
    }
}
