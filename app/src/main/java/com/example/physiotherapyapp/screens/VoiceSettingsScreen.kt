package com.example.physiotherapyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.physiotherapyapp.components.EnhancedCard
import com.example.physiotherapyapp.services.VoiceSettings

/**
 * Sesli yönlendirme ayarları ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsScreen(
    currentSettings: VoiceSettings,
    onSettingsChange: (VoiceSettings) -> Unit,
    onBackClick: () -> Unit,
    onTestVoice: () -> Unit
) {
    var settings by remember { mutableStateOf(currentSettings) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sesli Yönlendirme Ayarları",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ana ayarlar
            MainSettingsCard(
                settings = settings,
                onSettingsChange = { newSettings ->
                    settings = newSettings
                    onSettingsChange(newSettings)
                }
            )
            
            // Ses ayarları
            VoiceParametersCard(
                settings = settings,
                onSettingsChange = { newSettings ->
                    settings = newSettings
                    onSettingsChange(newSettings)
                },
                onTestVoice = onTestVoice
            )
            
            // Duyuru ayarları
            AnnouncementSettingsCard(
                settings = settings,
                onSettingsChange = { newSettings ->
                    settings = newSettings
                    onSettingsChange(newSettings)
                }
            )
            
            // Test butonu
            TestVoiceCard(
                onTestVoice = onTestVoice,
                isEnabled = settings.isEnabled
            )
        }
    }
}

/**
 * Ana ayarlar kartı
 */
@Composable
private fun MainSettingsCard(
    settings: VoiceSettings,
    onSettingsChange: (VoiceSettings) -> Unit
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
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sesli Yönlendirme",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sesli yönlendirmeyi etkinleştir",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = settings.isEnabled,
                    onCheckedChange = { 
                        onSettingsChange(settings.copy(isEnabled = it))
                    }
                )
            }
        }
    }
}

/**
 * Ses parametreleri kartı
 */
@Composable
private fun VoiceParametersCard(
    settings: VoiceSettings,
    onSettingsChange: (VoiceSettings) -> Unit,
    onTestVoice: () -> Unit
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
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ses Ayarları",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Konuşma hızı
            Text(
                text = "Konuşma Hızı: ${String.format("%.1f", settings.speechRate)}x",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = settings.speechRate,
                onValueChange = { 
                    onSettingsChange(settings.copy(speechRate = it))
                },
                valueRange = 0.5f..2.0f,
                steps = 14, // 0.1 adımlar
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ses tonu
            Text(
                text = "Ses Tonu: ${String.format("%.1f", settings.speechPitch)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = settings.speechPitch,
                onValueChange = { 
                    onSettingsChange(settings.copy(speechPitch = it))
                },
                valueRange = 0.5f..2.0f,
                steps = 14, // 0.1 adımlar
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Duyuru ayarları kartı
 */
@Composable
private fun AnnouncementSettingsCard(
    settings: VoiceSettings,
    onSettingsChange: (VoiceSettings) -> Unit
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
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Duyuru Ayarları",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Başlangıç duyurusu
            SettingRow(
                title = "Seans başlangıcını duyur",
                description = "Seans başlarken hoş geldin mesajı",
                checked = settings.announceStart,
                onCheckedChange = { 
                    onSettingsChange(settings.copy(announceStart = it))
                }
            )
            
            // Tamamlanma duyurusu
            SettingRow(
                title = "Egzersiz tamamlanmasını duyur",
                description = "Her egzersiz bitişinde tebrik mesajı",
                checked = settings.announceComplete,
                onCheckedChange = { 
                    onSettingsChange(settings.copy(announceComplete = it))
                }
            )
            
            // Talimat verme
            SettingRow(
                title = "Egzersiz talimatları ver",
                description = "Her egzersiz için nasıl yapılacağını anlat",
                checked = settings.giveInstructions,
                onCheckedChange = { 
                    onSettingsChange(settings.copy(giveInstructions = it))
                }
            )
            
            // Motivasyon mesajları
            SettingRow(
                title = "Motivasyon mesajları",
                description = "Ara sıra motivasyon verici sözler söyle",
                checked = settings.motivationalMessages,
                onCheckedChange = { 
                    onSettingsChange(settings.copy(motivationalMessages = it))
                }
            )
        }
    }
}

/**
 * Ayar satırı bileşeni
 */
@Composable
private fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Ses testi kartı
 */
@Composable
private fun TestVoiceCard(
    onTestVoice: () -> Unit,
    isEnabled: Boolean
) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Ses Testi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sesli yönlendirmenin nasıl çalıştığını test edin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onTestVoice,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Et")
            }
        }
    }
}
