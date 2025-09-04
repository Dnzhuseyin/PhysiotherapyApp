package com.example.physiotherapyapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiotherapyapp.components.EnhancedCard
import com.example.physiotherapyapp.components.GradientButton
import com.example.physiotherapyapp.services.*
import com.example.physiotherapyapp.ui.theme.*

/**
 * Kullanıcı profilleme anket ekranı
 * İlk giriş sırasında kullanıcının profilini belirler
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilingScreen(
    onProfilingComplete: (UserProfile) -> Unit,
    onSkip: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf<UserCategory?>(null) }
    var selectedAgeGroup by remember { mutableStateOf<AgeGroup?>(null) }
    var selectedActivityLevel by remember { mutableStateOf<ActivityLevel?>(null) }
    var primaryComplaint by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var selectedLimitations by remember { mutableStateOf(setOf<String>()) }
    
    val totalSteps = 6
    val progress = (currentStep + 1).toFloat() / totalSteps

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Kişisel Profil Oluştur",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Adım ${currentStep + 1} / $totalSteps",
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
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Step Content
            when (currentStep) {
                0 -> CategoryStep(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
                1 -> AgeGroupStep(
                    selectedAgeGroup = selectedAgeGroup,
                    onAgeGroupSelected = { selectedAgeGroup = it }
                )
                2 -> ActivityLevelStep(
                    selectedActivityLevel = selectedActivityLevel,
                    onActivityLevelSelected = { selectedActivityLevel = it }
                )
                3 -> ComplaintStep(
                    primaryComplaint = primaryComplaint,
                    onComplaintChanged = { primaryComplaint = it },
                    category = selectedCategory
                )
                4 -> GoalStep(
                    goal = goal,
                    onGoalChanged = { goal = it },
                    category = selectedCategory
                )
                5 -> LimitationsStep(
                    selectedLimitations = selectedLimitations,
                    onLimitationsChanged = { selectedLimitations = it }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Geri")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                
                val isStepValid = when (currentStep) {
                    0 -> selectedCategory != null
                    1 -> selectedAgeGroup != null
                    2 -> selectedActivityLevel != null
                    3 -> primaryComplaint.isNotBlank()
                    4 -> goal.isNotBlank()
                    5 -> true // Limitations opsiyonel
                    else -> false
                }
                
                GradientButton(
                    text = if (currentStep == totalSteps - 1) "🎯 Profili Tamamla" else "İleri",
                    onClick = {
                        if (currentStep == totalSteps - 1) {
                            // Profili oluştur ve tamamla
                            val profile = UserProfile(
                                category = selectedCategory!!,
                                ageGroup = selectedAgeGroup!!,
                                activityLevel = selectedActivityLevel!!,
                                primaryComplaint = primaryComplaint,
                                goal = goal,
                                limitations = selectedLimitations.toList()
                            )
                            onProfilingComplete(profile)
                        } else {
                            currentStep++
                        }
                    },
                    modifier = Modifier.weight(if (currentStep > 0) 1f else 2f),
                    icon = if (currentStep == totalSteps - 1) Icons.Default.Check else Icons.Default.ArrowForward,
                    colors = listOf(HealthyBlue40, MedicalGreen40),
                    enabled = isStepValid
                )
            }
        }
    }
}

/**
 * Kategori seçim adımı
 */
@Composable
private fun CategoryStep(
    selectedCategory: UserCategory?,
    onCategorySelected: (UserCategory) -> Unit
) {
    StepContainer(
        title = "Size en uygun kategori hangisi?",
        subtitle = "Bu seçim size özel egzersiz programları sunmamızı sağlar"
    ) {
        val categories = listOf(
            CategoryOption(
                category = UserCategory.ATHLETE,
                icon = Icons.Default.FitnessCenter,
                title = "Sporcu",
                description = "Aktif sporcu, performans odaklı",
                color = HealthyBlue40
            ),
            CategoryOption(
                category = UserCategory.POST_SURGERY,
                icon = Icons.Default.LocalHospital,
                title = "Ameliyat Sonrası",
                description = "Ameliyat sonrası iyileşme süreci",
                color = MedicalGreen40
            ),
            CategoryOption(
                category = UserCategory.ELDERLY,
                icon = Icons.Default.Elderly,
                title = "Yaşlı Birey",
                description = "65+ yaş, gentle egzersizler",
                color = WarmAccent40
            ),
            CategoryOption(
                category = UserCategory.GENERAL,
                icon = Icons.Default.Person,
                title = "Genel Kullanıcı",
                description = "Genel sağlık ve fitness",
                color = MaterialTheme.colorScheme.secondary
            )
        )
        
        categories.forEach { option ->
            CategoryCard(
                option = option,
                isSelected = selectedCategory == option.category,
                onClick = { onCategorySelected(option.category) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Yaş grubu seçim adımı
 */
@Composable
private fun AgeGroupStep(
    selectedAgeGroup: AgeGroup?,
    onAgeGroupSelected: (AgeGroup) -> Unit
) {
    StepContainer(
        title = "Yaş aralığınız nedir?",
        subtitle = "Yaşınıza uygun egzersiz yoğunluğu belirleyelim"
    ) {
        AgeGroup.values().forEach { ageGroup ->
            SelectionCard(
                title = ageGroup.displayName,
                subtitle = "${ageGroup.range} yaş arası",
                isSelected = selectedAgeGroup == ageGroup,
                onClick = { onAgeGroupSelected(ageGroup) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Aktivite seviyesi adımı
 */
@Composable
private fun ActivityLevelStep(
    selectedActivityLevel: ActivityLevel?,
    onActivityLevelSelected: (ActivityLevel) -> Unit
) {
    StepContainer(
        title = "Mevcut aktivite seviyeniz?",
        subtitle = "Egzersiz yoğunluğunu buna göre ayarlayacağız"
    ) {
        val activityDescriptions = mapOf(
            ActivityLevel.SEDENTARY to "Çoğunlukla masa başında çalışıyorum",
            ActivityLevel.LIGHT to "Haftada 1-2 kez hafif egzersiz",
            ActivityLevel.MODERATE to "Haftada 3-4 kez düzenli egzersiz",
            ActivityLevel.HIGH to "Haftada 5+ kez yoğun egzersiz",
            ActivityLevel.ATHLETE to "Profesyonel/yarı-profesyonel sporcu"
        )
        
        ActivityLevel.values().forEach { level ->
            SelectionCard(
                title = level.displayName,
                subtitle = activityDescriptions[level] ?: "",
                isSelected = selectedActivityLevel == level,
                onClick = { onActivityLevelSelected(level) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Ana şikayet adımı
 */
@Composable
private fun ComplaintStep(
    primaryComplaint: String,
    onComplaintChanged: (String) -> Unit,
    category: UserCategory?
) {
    val suggestions = when (category) {
        UserCategory.ATHLETE -> listOf(
            "Kas yorgunluğu", "Performans düşüklüğü", "Yaralanma önleme",
            "Esneklik artırma", "Güç geliştirme"
        )
        UserCategory.POST_SURGERY -> listOf(
            "Ameliyat sonrası ağrı", "Hareket kısıtlılığı", "Kas zayıflığı",
            "Eklem sertliği", "İyileşme süreci"
        )
        UserCategory.ELDERLY -> listOf(
            "Denge problemleri", "Eklem ağrıları", "Kas güçsüzlüğü",
            "Hareket zorluğu", "Esneklik kaybı"
        )
        else -> listOf(
            "Bel ağrısı", "Boyun ağrısı", "Omuz sertliği",
            "Kas gerginliği", "Genel kondisyon eksikliği"
        )
    }
    
    StepContainer(
        title = "Ana şikayetiniz nedir?",
        subtitle = "Size en uygun egzersizleri önerebilmek için"
    ) {
        OutlinedTextField(
            value = primaryComplaint,
            onValueChange = onComplaintChanged,
            label = { Text("Ana şikayetinizi yazın") },
            placeholder = { Text("Örnek: Bel ağrısı") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Yaygın şikayetler:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        suggestions.forEach { suggestion ->
            SuggestionChip(
                text = suggestion,
                onClick = { onComplaintChanged(suggestion) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Hedef belirleme adımı
 */
@Composable
private fun GoalStep(
    goal: String,
    onGoalChanged: (String) -> Unit,
    category: UserCategory?
) {
    val suggestions = when (category) {
        UserCategory.ATHLETE -> listOf(
            "Performans artırma", "Yaralanma önleme", "Güç geliştirme",
            "Dayanıklılık artırma", "İyileşme hızlandırma"
        )
        UserCategory.POST_SURGERY -> listOf(
            "Ağrıyı azaltma", "Hareket genişliği artırma", "Güç geri kazanma",
            "Normal yaşama dönüş", "İyileşme hızlandırma"
        )
        UserCategory.ELDERLY -> listOf(
            "Denge iyileştirme", "Günlük aktiviteler", "Düşme önleme",
            "Bağımsızlık koruma", "Yaşam kalitesi artırma"
        )
        else -> listOf(
            "Ağrı azaltma", "Fitness artırma", "Stres azaltma",
            "Esneklik kazanma", "Genel sağlık"
        )
    }
    
    StepContainer(
        title = "Hedefiniz nedir?",
        subtitle = "Bu hedefe yönelik program hazırlayacağız"
    ) {
        OutlinedTextField(
            value = goal,
            onValueChange = onGoalChanged,
            label = { Text("Hedefinizi yazın") },
            placeholder = { Text("Örnek: Bel ağrımı azaltmak istiyorum") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Yaygın hedefler:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        suggestions.forEach { suggestion ->
            SuggestionChip(
                text = suggestion,
                onClick = { onGoalChanged(suggestion) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Kısıtlamalar adımı
 */
@Composable
private fun LimitationsStep(
    selectedLimitations: Set<String>,
    onLimitationsChanged: (Set<String>) -> Unit
) {
    val limitations = listOf(
        "Diz problemleri", "Bel problemleri", "Omuz problemleri",
        "Boyun problemleri", "Kalp hastalığı", "Yüksek tansiyon",
        "Denge problemi", "Geçirilmiş ameliyat", "Kronik ağrı",
        "Hareket kısıtlılığı"
    )
    
    StepContainer(
        title = "Kısıtlamalarınız var mı?",
        subtitle = "Güvenli egzersizler önerebilmek için (opsiyonel)"
    ) {
        Text(
            text = "Size uygun olmayan egzersizleri belirleyelim:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        limitations.forEach { limitation ->
            LimitationCheckItem(
                text = limitation,
                isChecked = selectedLimitations.contains(limitation),
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        onLimitationsChanged(selectedLimitations + limitation)
                    } else {
                        onLimitationsChanged(selectedLimitations - limitation)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (selectedLimitations.isEmpty()) {
            Text(
                text = "✅ Harika! Kısıtlama seçilmedi",
                style = MaterialTheme.typography.bodyMedium,
                color = SuccessGreen,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

/**
 * Step container wrapper
 */
@Composable
private fun StepContainer(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        item {
            Column(content = content)
        }
    }
}

/**
 * Kategori kartı
 */
@Composable
private fun CategoryCard(
    option: CategoryOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) option.color.copy(alpha = 0.3f) 
                           else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(3.dp, option.color) 
        else 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isSelected) option.color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) option.color else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seçili",
                    tint = option.color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Seçim kartı
 */
@Composable
private fun SelectionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                           else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary) 
        else 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                           else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seçili",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Öneri chip'i
 */
@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Kısıtlama check item
 */
@Composable
private fun LimitationCheckItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                           else MaterialTheme.colorScheme.surface
        ),
        border = if (isChecked) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isChecked) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                color = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Kategori seçeneği data class'ı
 */
private data class CategoryOption(
    val category: UserCategory,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: androidx.compose.ui.graphics.Color
)
