package com.example.physiotherapyapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.example.physiotherapyapp.components.*
import com.example.physiotherapyapp.data.Exercise
import com.example.physiotherapyapp.data.Session
import com.example.physiotherapyapp.ui.theme.*

/**
 * Aktif seans ekranı - egzersizlerin sırayla yapıldığı ekran
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    session: Session?,
    isCurrentExerciseCompleted: Boolean,
    areAllExercisesCompleted: Boolean,
    onStartExercise: () -> Unit,
    onCompleteExercise: () -> Unit,
    onCompleteSession: () -> Unit,
    onCancelSession: () -> Unit,
    onRepeatInstruction: () -> Unit = {},
    onGiveMotivation: () -> Unit = {},
    onStopVoice: () -> Unit = {}
) {
    if (session == null) {
        // Hata durumu
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Seans bulunamadı")
            Button(onClick = onCancelSession) {
                Text("Ana Sayfaya Dön")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = session.templateName,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onCancelSession) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Seansı İptal Et"
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (areAllExercisesCompleted) {
                // Tüm egzersizler tamamlandı - tebrik ekranı
                CompletionScreen(
                    sessionName = session.templateName,
                    onCompleteSession = onCompleteSession
                )
            } else {
                // Aktif egzersiz gösterimi
                ActiveExerciseScreen(
                    session = session,
                    isCurrentExerciseCompleted = isCurrentExerciseCompleted,
                    onStartExercise = onStartExercise,
                    onCompleteExercise = onCompleteExercise
                )
            }
        }
    }
}

/**
 * Aktif egzersiz gösterim bileşeni
 */
@Composable
private fun ActiveExerciseScreen(
    session: Session,
    isCurrentExerciseCompleted: Boolean,
    onStartExercise: () -> Unit,
    onCompleteExercise: () -> Unit
) {
    val currentExercise = session.exercises[session.currentExerciseIndex]
    
    // Gelişmiş ilerleme göstergesi
    AnimatedProgressBar(
        progress = (session.currentExerciseIndex + 1).toFloat() / session.exercises.size,
        modifier = Modifier.fillMaxWidth(),
        height = 12,
        progressColor = MedicalGreen40,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Egzersiz ${session.currentExerciseIndex + 1} / ${session.exercises.size}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    // Mevcut egzersiz kartı - Gelişmiş tasarım
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = if (isCurrentExerciseCompleted) {
                    Brush.linearGradient(
                        colors = listOf(MedicalGreen40, HealthyBlue40)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animasyonlu egzersiz durumu ikonu
            val infiniteTransition = rememberInfiniteTransition(label = "exercise_icon")
            val iconScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isCurrentExerciseCompleted) 1f else 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "icon_scale"
            )
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCurrentExerciseCompleted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = "Egzersiz Durumu",
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer(scaleX = iconScale, scaleY = iconScale),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Egzersiz adı
            Text(
                text = currentExercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Egzersiz açıklaması
            if (currentExercise.description.isNotEmpty()) {
                Text(
                    text = currentExercise.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Durum mesajı
            Text(
                text = if (isCurrentExerciseCompleted) {
                    "✓ Tamamlandı - Harika iş!"
                } else {
                    "Egzersizi yapmaya hazır olduğunuzda başlatın"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    // Aksiyon butonları - Gradient
    if (!isCurrentExerciseCompleted) {
        GradientButton(
            text = "Egzersizi Başlat",
            onClick = onStartExercise,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.PlayArrow,
            colors = listOf(HealthyBlue40, MedicalGreen40)
        )
    } else {
        GradientButton(
            text = "Sonrakine Geç",
            onClick = onCompleteExercise,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.CheckCircle,
            colors = listOf(MedicalGreen40, SuccessGreen)
        )
    }
}

/**
 * Seans tamamlanma ekranı - Gelişmiş animasyonlu
 */
@Composable
private fun CompletionScreen(
    sessionName: String,
    onCompleteSession: () -> Unit
) {
    var showSuccess by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        showSuccess = true
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Başarı animasyonu
        SuccessAnimation(
            isVisible = showSuccess,
            modifier = Modifier.padding(16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Animasyonlu tebrik mesajı
        AnimatedVisibility(
            visible = showSuccess,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 500)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 500))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tebrikler! 🎉",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "\"$sessionName\" seansını başarıyla tamamladınız!",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Puan kazancı kartı
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(WarmAccent40, WarmAccent80)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "+10 PUAN KAZANDINIZ!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Animasyonlu tamamla butonu
        AnimatedVisibility(
            visible = showSuccess,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 1000)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 1000))
        ) {
            GradientButton(
                text = "Ana Sayfaya Dön",
                onClick = onCompleteSession,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.CheckCircle,
                colors = listOf(SuccessGreen, MedicalGreen40)
            )
        }
    }
} 