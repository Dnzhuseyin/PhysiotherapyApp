package com.example.physiotherapyapp.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiotherapyapp.data.Exercise
import com.example.physiotherapyapp.data.Session

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
    onCancelSession: () -> Unit
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
    
    // İlerleme göstergesi
    LinearProgressIndicator(
        progress = { (session.currentExerciseIndex + 1).toFloat() / session.exercises.size },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Egzersiz ${session.currentExerciseIndex + 1} / ${session.exercises.size}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    // Mevcut egzersiz kartı
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Egzersiz durumu ikonu
            Icon(
                imageVector = if (isCurrentExerciseCompleted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = "Egzersiz Durumu",
                modifier = Modifier.size(64.dp),
                tint = if (isCurrentExerciseCompleted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Egzersiz adı
            Text(
                text = currentExercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Egzersiz açıklaması
            if (currentExercise.description.isNotEmpty()) {
                Text(
                    text = currentExercise.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Durum mesajı
            Text(
                text = if (isCurrentExerciseCompleted) {
                    "✓ Tamamlandı"
                } else {
                    "Egzersizi yapmaya hazır olduğunuzda başlatın"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (isCurrentExerciseCompleted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                },
                fontWeight = if (isCurrentExerciseCompleted) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    // Aksiyon butonları
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isCurrentExerciseCompleted) {
            Button(
                onClick = onStartExercise,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Başlat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Button(
                onClick = onCompleteExercise,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sonrakine Geç",
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Seans tamamlanma ekranı
 */
@Composable
private fun CompletionScreen(
    sessionName: String,
    onCompleteSession: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Tebrik ikonu
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Tebrikler",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tebrik mesajı
        Text(
            text = "Tebrikler!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Seansınızı başarıyla tamamladınız!",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "10 puan kazandınız 🎉",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Tamamla butonu
        Button(
            onClick = onCompleteSession,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Seansı Tamamla",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 