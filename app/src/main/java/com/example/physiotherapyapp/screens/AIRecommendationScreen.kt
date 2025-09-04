package com.example.physiotherapyapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiotherapyapp.components.EnhancedCard
import com.example.physiotherapyapp.components.GradientButton
import com.example.physiotherapyapp.data.Exercise
import com.example.physiotherapyapp.data.SessionTemplate
import com.example.physiotherapyapp.services.*
import com.example.physiotherapyapp.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*

/**
 * AI destekli akıllı program önerisi ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRecommendationScreen(
    userProfile: UserProfile,
    currentPainLevel: Int? = null,
    previousSessions: List<String> = emptyList(),
    onRecommendationAccepted: (SessionTemplate) -> Unit,
    onBackClick: () -> Unit,
    onRegenerateRequest: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var recommendation by remember { mutableStateOf<AISessionRecommendation?>(null) }
    var improvements by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val aiService = remember { AIRecommendationService() }
    
    // AI önerisi al
    LaunchedEffect(userProfile) {
        try {
            isLoading = true
            error = null
            
            // Gerçek AI çağrısı (simüle edilmiş gecikmeli)
            delay(2000) // Gerçekçi yükleme süresi
            
            recommendation = aiService.generateSessionRecommendation(
                userProfile = userProfile,
                currentPainLevel = currentPainLevel,
                previousSessions = previousSessions
            )
            
            // İyileştirme önerilerini al
            improvements = aiService.generateImprovementSuggestions(
                userProfile = userProfile,
                painHistory = emptyList(), // Mevcut ağrı geçmişi
                completedExercises = previousSessions
            )
            
        } catch (e: Exception) {
            error = "AI önerisi alınırken hata oluştu: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🤖 AI Akıllı Öneriler",
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
                    if (!isLoading && recommendation != null) {
                        IconButton(
                            onClick = {
                                // Yeniden generate et
                                isLoading = true
                                onRegenerateRequest()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yeniden Oluştur"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        
        when {
            isLoading -> LoadingContent(modifier = Modifier.padding(paddingValues))
            error != null -> ErrorContent(
                error = error!!,
                onRetry = onRegenerateRequest,
                modifier = Modifier.padding(paddingValues)
            )
            recommendation != null -> RecommendationContent(
                recommendation = recommendation!!,
                improvements = improvements,
                userProfile = userProfile,
                onAccept = { 
                    // AI önerisini SessionTemplate'e çevir
                    val template = recommendation!!.toSessionTemplate()
                    onRecommendationAccepted(template)
                },
                onRegenerate = {
                    isLoading = true
                    onRegenerateRequest()
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Yükleme içeriği
 */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animasyonlu AI ikonu
        var isAnimating by remember { mutableStateOf(true) }
        val rotation by animateFloatAsState(
            targetValue = if (isAnimating) 360f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ai_rotation"
        )
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(HealthyBlue40, MedicalGreen40)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .graphicsLayer { rotationZ = rotation },
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "🤖 AI Sihri Çalışıyor...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val loadingMessages = listOf(
            "Profiliniz analiz ediliyor...",
            "En uygun egzersizler seçiliyor...",
            "Kişisel program hazırlanıyor...",
            "Son dokunuşlar yapılıyor..."
        )
        
        var currentMessageIndex by remember { mutableIntStateOf(0) }
        
        LaunchedEffect(Unit) {
            while (true) {
                delay(800)
                currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
            }
        }
        
        AnimatedContent(
            targetState = loadingMessages[currentMessageIndex],
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "loading_message"
        ) { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Hata içeriği
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🤖💫",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "AI Bağlantı Sorunu",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        GradientButton(
            text = "Tekrar Dene",
            onClick = onRetry,
            icon = Icons.Default.Refresh,
            colors = listOf(HealthyBlue40, MedicalGreen40)
        )
    }
}

/**
 * Öneri içeriği
 */
@Composable
private fun RecommendationContent(
    recommendation: AISessionRecommendation,
    improvements: List<String>,
    userProfile: UserProfile,
    onAccept: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // AI güven göstergesi
            AIConfidenceCard(confidence = recommendation.confidence)
        }
        
        item {
            // Ana öneri kartı
            MainRecommendationCard(
                recommendation = recommendation,
                userProfile = userProfile
            )
        }
        
        item {
            // Egzersiz detayları
            ExerciseDetailsCard(exercises = recommendation.exercises)
        }
        
        if (recommendation.specialNotes.isNotBlank()) {
            item {
                SpecialNotesCard(notes = recommendation.specialNotes)
            }
        }
        
        if (improvements.isNotEmpty()) {
            item {
                ImprovementSuggestionsCard(suggestions = improvements)
            }
        }
        
        item {
            // Aksiyon butonları
            ActionButtonsSection(
                onAccept = onAccept,
                onRegenerate = onRegenerate
            )
        }
    }
}

/**
 * AI güven göstergesi kartı
 */
@Composable
private fun AIConfidenceCard(confidence: Float) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🤖 AI Güven Oranı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bu öneri %${(confidence * 100).toInt()} güvenilirlik ile oluşturuldu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Güven göstergesi
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(getConfidenceColor(confidence).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%${(confidence * 100).toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = getConfidenceColor(confidence)
                )
            }
        }
    }
}

/**
 * Ana öneri kartı
 */
@Composable
private fun MainRecommendationCard(
    recommendation: AISessionRecommendation,
    userProfile: UserProfile
) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = recommendation.sessionName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${userProfile.category.displayName} için özelleştirilmiş",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = "⏱️",
                    text = recommendation.estimatedDuration
                )
                
                InfoChip(
                    icon = "💪",
                    text = "${recommendation.exercises.size} egzersiz"
                )
                
                InfoChip(
                    icon = "🎯",
                    text = userProfile.category.displayName
                )
            }
        }
    }
}

/**
 * Egzersiz detayları kartı
 */
@Composable
private fun ExerciseDetailsCard(exercises: List<String>) {
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
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "💪 Egzersiz Listesi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            exercises.forEachIndexed { index, exercise ->
                ExerciseListItem(
                    index = index + 1,
                    exerciseName = exercise
                )
                if (index < exercises.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Egzersiz liste öğesi
 */
@Composable
private fun ExerciseListItem(
    index: Int,
    exerciseName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = exerciseName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Özel notlar kartı
 */
@Composable
private fun SpecialNotesCard(notes: String) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = WarmAccent40.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = WarmAccent40
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "⚠️ Önemli Notlar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarmAccent40
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * İyileştirme önerileri kartı
 */
@Composable
private fun ImprovementSuggestionsCard(suggestions: List<String>) {
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
                    imageVector = Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "💡 AI İyileştirme Önerileri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Aksiyon butonları bölümü
 */
@Composable
private fun ActionButtonsSection(
    onAccept: () -> Unit,
    onRegenerate: () -> Unit
) {
    Column {
        GradientButton(
            text = "✨ Bu Programı Kullan",
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.PlayArrow,
            colors = listOf(HealthyBlue40, MedicalGreen40)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onRegenerate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("🔄 Farklı Öneri İste")
        }
    }
}

/**
 * Info chip bileşeni
 */
@Composable
private fun InfoChip(
    icon: String,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Güven oranına göre renk döndürür
 */
private fun getConfidenceColor(confidence: Float): androidx.compose.ui.graphics.Color {
    return when {
        confidence >= 0.8f -> SuccessGreen
        confidence >= 0.6f -> WarmAccent40
        else -> ErrorRed
    }
}

/**
 * AI önerisini SessionTemplate'e çevirir
 */
private fun AISessionRecommendation.toSessionTemplate(): SessionTemplate {
    val exerciseList = this.exercises.map { exerciseName ->
        Exercise(
            name = exerciseName,
            description = "AI tarafından önerilen egzersiz",
            isCompleted = false
        )
    }
    
    return SessionTemplate(
        name = this.sessionName,
        exercises = exerciseList,
        createdDate = Date(),
        estimatedDuration = this.estimatedDuration
    )
}
