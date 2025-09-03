package com.example.physiotherapyapp.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiotherapyapp.ui.theme.*

/**
 * Gradient buton bileşeni
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    colors: List<Color> = listOf(HealthyBlue40, MedicalGreen40)
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (enabled) 8.dp else 2.dp,
        animationSpec = tween(300),
        label = "button_elevation"
    )
    
    Box(
        modifier = modifier
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(16.dp),
                spotColor = HealthyBlue40.copy(alpha = 0.25f)
            )
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(colors)
                } else {
                    Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Gelişmiş kart bileşeni
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Int = 6,
    cornerRadius: Int = 20,
    content: @Composable () -> Unit
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (onClick != null) elevation.dp else (elevation - 2).dp,
        animationSpec = tween(300),
        label = "card_elevation"
    )
    
    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation
        )
    ) {
        content()
    }
}

/**
 * İstatistik kartı
 */
@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(500)
        ) + fadeIn(animationSpec = tween(500))
    ) {
        EnhancedCard(
            modifier = modifier.height(140.dp),
            backgroundColor = backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // İkon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Değer
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                // Başlık
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Animasyonlu ilerleme çubuğu
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    height: Int = 8
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress_animation"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(height.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(progressColor, progressColor.copy(alpha = 0.8f))
                    ),
                    shape = RoundedCornerShape(height.dp)
                )
        )
    }
}

/**
 * Motivasyon kartı
 */
@Composable
fun MotivationCard(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(600)
        ) + fadeIn(animationSpec = tween(600))
    ) {
        EnhancedCard(
            modifier = modifier,
            backgroundColor = backgroundColor
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animasyonlu ikon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = contentColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = contentColor
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Floating Action Button özelleştirilmiş
 */
@Composable
fun EnhancedFAB(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
        },
        text = {
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 12.dp,
            pressedElevation = 16.dp
        )
    )
}

/**
 * Başarı animasyonu
 */
@Composable
fun SuccessAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = SuccessGreen.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .border(
                    width = 4.dp,
                    color = SuccessGreen,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Başarılı",
                modifier = Modifier.size(64.dp),
                tint = SuccessGreen
            )
        }
    }
}
