package com.example.physiotherapyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Ana ekran - kullanıcının temel seçenekleri görebildiği ekran
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onExerciseSelectionClick: () -> Unit,
    onSessionHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Fizik Tedavi",
                        fontWeight = FontWeight.Bold
                    )
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
            // Hoşgeldin mesajı
            Text(
                text = "Bugün hangi egzersizleri yapmaya hazırsınız?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Ana menü seçenekleri
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Egzersiz Seç ve Seansı Başlat
                MenuCard(
                    title = "Egzersiz Seç ve Seansı Başlat",
                    description = "Egzersizlerinizi seçin ve antrenmanınızı başlatın",
                    icon = Icons.Default.PlayArrow,
                    onClick = onExerciseSelectionClick
                )
                
                // Seans Geçmişi
                MenuCard(
                    title = "Seans Geçmişi",
                    description = "Geçmiş antrenmanlarınızı görüntüleyin",
                    icon = Icons.Default.History,
                    onClick = onSessionHistoryClick
                )
                
                // Profilim
                MenuCard(
                    title = "Profilim",
                    description = "İlerlemenizi ve puanlarınızı görün",
                    icon = Icons.Default.Person,
                    onClick = onProfileClick
                )
            }
        }
    }
}

/**
 * Menü seçeneklerini gösteren kart bileşeni
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Metin bilgileri
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 