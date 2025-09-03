package com.example.physiotherapyapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiotherapyapp.components.*
import com.example.physiotherapyapp.data.SessionTemplate
import com.example.physiotherapyapp.ui.theme.*

/**
 * Egzersiz yap ekranı - kayıtlı seans şablonlarını gösterir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    sessionTemplates: List<SessionTemplate>,
    onStartSession: (SessionTemplate) -> Unit,
    onCreateNewSession: () -> Unit,
    onDeleteSession: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Egzersiz Yap",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            )
        },
        floatingActionButton = {
            EnhancedFAB(
                text = "Yeni Seans",
                icon = Icons.Default.Add,
                onClick = onCreateNewSession
            )
        }
    ) { paddingValues ->
        if (sessionTemplates.isEmpty()) {
            EmptyWorkoutScreen(onCreateNewSession = onCreateNewSession)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Başlık kartı
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Hazırladığınız seansları burada bulabilirsiniz",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "${sessionTemplates.size} seans mevcut",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Seans şablonları listesi - Animasyonlu
                itemsIndexed(sessionTemplates) { index, template ->
                    var isVisible by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(key1 = template.id) {
                        kotlinx.coroutines.delay(index * 100L) // Sıralı animasyon
                        isVisible = true
                    }
                    
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(500, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        SessionTemplateCard(
                            template = template,
                            onStartSession = { onStartSession(template) },
                            onDeleteSession = { onDeleteSession(template.id) }
                        )
                    }
                }
                
                // Alt boşluk (FAB için)
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Boş workout ekranı
 */
@Composable
private fun EmptyWorkoutScreen(
    onCreateNewSession: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Henüz hiç seans oluşturmadınız",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "İlk seansınızı oluşturmak için aşağıdaki butona tıklayın",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onCreateNewSession,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "İlk Seansımı Oluştur",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Seans şablonu kartı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionTemplateCard(
    template: SessionTemplate,
    onStartSession: () -> Unit,
    onDeleteSession: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Başlık satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bilgi satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.FitnessCenter,
                    text = "${template.exercises.size} egzersiz"
                )
                InfoChip(
                    icon = Icons.Default.Schedule,
                    text = template.estimatedDuration
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Egzersiz listesi
            Text(
                text = "Egzersizler:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            template.exercises.take(3).forEach { exercise ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (template.exercises.size > 3) {
                Text(
                    text = "ve ${template.exercises.size - 3} egzersiz daha...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 24.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Başlat butonu - Gradient
            GradientButton(
                text = "Seansı Başlat",
                onClick = onStartSession,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.PlayArrow,
                colors = listOf(HealthyBlue40, MedicalGreen40)
            )
        }
    }
    
    // Silme onay dialogu
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Seansı Sil",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "\"${template.name}\" seansını silmek istediğinizden emin misiniz? Bu işlem geri alınamaz."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}

/**
 * Bilgi chip bileşeni
 */
@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 