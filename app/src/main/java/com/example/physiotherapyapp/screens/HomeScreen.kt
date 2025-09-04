package com.example.physiotherapyapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.physiotherapyapp.components.*
import com.example.physiotherapyapp.data.SessionTemplate
import com.example.physiotherapyapp.data.User
import com.example.physiotherapyapp.navigation.NavigationRoutes
import com.example.physiotherapyapp.ui.theme.*

/**
 * Ana ekran - dashboard görünümü
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    recentTemplates: List<SessionTemplate> = emptyList(),
    onCreateNewSession: () -> Unit,
    navController: NavController
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Fizik Tedavi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hoşgeldin kartı
            WelcomeCard(user = user)
            
            // Hızlı istatistikler
            QuickStatsRow(user = user)
            
            // Hızlı aksiyonlar
            QuickActionsCard(
                onCreateNewSession = onCreateNewSession,
                navController = navController
            )
            
            // Son seanslar (eğer varsa)
            if (recentTemplates.isNotEmpty()) {
                RecentSessionsSection(
                    templates = recentTemplates.take(3)
                )
            }
            
            // Motivasyon kartı
            MotivationCard(totalSessions = user.totalSessions)
        }
    }
}

/**
 * Hoşgeldin kartı - Gelişmiş gradient arka plan
 */
@Composable
private fun WelcomeCard(user: User) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(800, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(800))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animasyonlu ikon
                val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
                val iconScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "icon_scale"
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer(scaleX = iconScale, scaleY = iconScale),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = getGreetingMessage(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Bugün hangi hedeflerinizi gerçekleştirmeye hazırsınız?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Hızlı istatistikler satırı - Animasyonlu
 */
@Composable
private fun QuickStatsRow(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatisticCard(
            modifier = Modifier.weight(1f),
            title = "Toplam Seans",
            value = user.totalSessions.toString(),
            icon = Icons.Default.EmojiEvents,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            iconColor = MedicalGreen40
        )
        
        StatisticCard(
            modifier = Modifier.weight(1f),
            title = "Toplam Puan",
            value = user.totalPoints.toString(),
            icon = Icons.Default.Star,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconColor = WarmAccent40
        )
    }
}

/**
 * İstatistik kartı
 */
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = contentColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Hızlı aksiyonlar kartı - Gelişmiş tasarım
 */
@Composable
private fun QuickActionsCard(
    onCreateNewSession: () -> Unit,
    navController: NavController
) {
    EnhancedCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = HealthyBlue40
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Hızlı Başlat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            GradientButton(
                text = "Yeni Seans Oluştur",
                onClick = onCreateNewSession,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Add,
                colors = listOf(HealthyBlue40, MedicalGreen40)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientButton(
                    text = "Analiz",
                    onClick = { navController.navigate(NavigationRoutes.ANALYTICS) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Analytics,
                    colors = listOf(MedicalGreen40, HealthyBlue40)
                )
                
                GradientButton(
                    text = "Ağrı Raporları",
                    onClick = { navController.navigate(NavigationRoutes.PAIN_REPORTS) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalHospital,
                    colors = listOf(WarmAccent40, ErrorRed.copy(alpha = 0.8f))
                )
            }
        }
    }
}

/**
 * Son seanslar bölümü
 */
@Composable
private fun RecentSessionsSection(
    templates: List<SessionTemplate>
) {
    Column {
        Text(
            text = "Son Seanslarınız",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(templates) { template ->
                RecentSessionCard(template = template)
            }
        }
    }
}

/**
 * Son seans kartı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentSessionCard(
    template: SessionTemplate
) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${template.exercises.size} egzersiz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Text(
                text = template.estimatedDuration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Motivasyon kartı - Gelişmiş animasyonlu
 */
@Composable
private fun MotivationCard(totalSessions: Int) {
    val (message, icon) = getMotivationData(totalSessions)
    
    MotivationCard(
        message = message,
        icon = icon,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
}

/**
 * Selamlama mesajı getirir
 */
private fun getGreetingMessage(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Günaydın! 🌅"
        hour < 17 -> "İyi öğleden sonralar! ☀️"
        else -> "İyi akşamlar! 🌙"
    }
}

/**
 * Motivasyon verisi getirir
 */
private fun getMotivationData(totalSessions: Int): Pair<String, ImageVector> {
    return when {
        totalSessions == 0 -> Pair(
            "İlk adımı atmaya hazır mısınız? Başlayalım! 💪",
            Icons.Default.EmojiEvents
        )
        totalSessions < 5 -> Pair(
            "Harika gidiyorsunuz! Momentum'unuzu koruyun! 🚀",
            Icons.Default.TrendingUp
        )
        totalSessions < 10 -> Pair(
            "Muhteşem ilerleme! Kendinizle gurur duyabilirsiniz! 🌟",
            Icons.Default.Star
        )
        totalSessions < 20 -> Pair(
            "Siz gerçek bir şampiyon olma yolundasınız! 🏆",
            Icons.Default.EmojiEvents
        )
        else -> Pair(
            "İnanılmaz! Siz bir efsanesiniz! Devam edin! 🔥",
            Icons.Default.Whatshot
        )
    }
} 