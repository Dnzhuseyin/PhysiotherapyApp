package com.example.physiotherapyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.physiotherapyapp.components.BottomNavigationBar
import com.example.physiotherapyapp.navigation.NavigationRoutes
import com.example.physiotherapyapp.screens.*
import com.example.physiotherapyapp.ui.theme.PhysiotherapyAppTheme
import com.example.physiotherapyapp.viewmodel.PhysiotherapyViewModel

/**
 * Ana Activity - Fizik Tedavi Uygulaması
 * 
 * Bu uygulama kullanıcıların fizik tedavi egzersizlerini seans mantığıyla 
 * takip edebilmesini sağlar. Tüm veriler geçici bellekte (ViewModel) tutulur.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhysiotherapyAppTheme {
                PhysiotherapyApp()
            }
        }
    }
}

/**
 * Ana uygulama Composable fonksiyonu
 * Navigation ve tüm ekranların yönetimini sağlar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysiotherapyApp() {
    // Navigation controller
    val navController = rememberNavController()
    
    // Context'i al
    val context = LocalContext.current
    
    // Ana ViewModel - Context ile başlat
    val viewModel: PhysiotherapyViewModel = viewModel {
        PhysiotherapyViewModel(context = context)
    }
    
    // ViewModel state'lerini observe et
    val user by viewModel.user
    val currentSession by viewModel.currentSession
    val sessionTemplates = viewModel.sessionTemplates
    val completedSessions = viewModel.completedSessions
    
    // Mevcut route'u kontrol et
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    // Bottom navigation gerektiren route'lar
    val bottomNavRoutes = listOf(
        NavigationRoutes.MAIN_HOME,
        NavigationRoutes.MAIN_WORKOUT,
        NavigationRoutes.MAIN_HISTORY,
        NavigationRoutes.MAIN_PROFILE
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Sadece ana ekranlarda bottom navigation göster
            if (currentRoute in bottomNavRoutes) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.LOGIN,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Giriş Ekranı
            composable(NavigationRoutes.LOGIN) {
                LoginScreen(
                    onContinueClick = {
                        // Profil olsun ya da olmasın direkt ana sayfaya git
                        navController.navigate(NavigationRoutes.MAIN_HOME) {
                            popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            
            // Ana Ekran (Dashboard)
            composable(NavigationRoutes.MAIN_HOME) {
                HomeScreen(
                    user = user,
                    recentTemplates = sessionTemplates,
                    onCreateNewSession = {
                        navController.navigate(NavigationRoutes.EXERCISE_SELECTION)
                    },
                    navController = navController
                )
            }
            
            // Egzersiz Yap Ekranı
            composable(NavigationRoutes.MAIN_WORKOUT) {
                WorkoutScreen(
                    sessionTemplates = sessionTemplates,
                    onStartSession = { template ->
                        viewModel.startSessionFromTemplate(template)
                        navController.navigate(NavigationRoutes.SESSION)
                    },
                    onCreateNewSession = {
                        navController.navigate(NavigationRoutes.EXERCISE_SELECTION)
                    },
                    onDeleteSession = { templateId ->
                        viewModel.deleteSessionTemplate(templateId)
                    }
                )
            }
            
            // Seans Geçmişi Ekranı
            composable(NavigationRoutes.MAIN_HISTORY) {
                SessionHistoryScreen(
                    completedSessions = completedSessions,
                    onBackClick = { /* Bottom nav kullanıldığı için geri buton yok */ }
                )
            }
            
            // Profil Ekranı
            composable(NavigationRoutes.MAIN_PROFILE) {
                ProfileScreen(
                    user = user,
                    onBackClick = { /* Bottom nav kullanıldığı için geri buton yok */ }
                )
            }
            
            // Yeni Seans Oluşturma Ekranı
            composable(NavigationRoutes.EXERCISE_SELECTION) {
                CreateSessionScreen(
                    availableExercises = viewModel.availableExercises,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCreateSession = { name, exercises ->
                        viewModel.createSessionTemplate(name, exercises)
                        navController.popBackStack()
                    }
                )
            }
            
            // Aktif Seans Ekranı
            composable(NavigationRoutes.SESSION) {
                SessionScreen(
                    session = currentSession,
                    isCurrentExerciseCompleted = viewModel.isCurrentExerciseCompleted(),
                    areAllExercisesCompleted = viewModel.areAllExercisesCompleted(),
                    onStartExercise = {
                        // Başlat butonuna basıldığında doğrudan tamamlanmış olarak işaretle
                        // (Gerçek uygulamada burada timer olabilir)
                        viewModel.completeCurrentExercise()
                    },
                    onCompleteExercise = {
                        viewModel.completeCurrentExercise()
                    },
                    onCompleteSession = {
                        val session = viewModel.currentSession.value
                        viewModel.completeSession()
                        
                        // Ağrı günlüğüne yönlendir
                        if (session != null) {
                            navController.navigate("${NavigationRoutes.PAIN_DIARY}/${session.id}/${session.templateName}")
                        } else {
                            navController.navigate(NavigationRoutes.MAIN_HOME) {
                                popUpTo(NavigationRoutes.MAIN_HOME) { inclusive = true }
                            }
                        }
                    },
                    onCancelSession = {
                        viewModel.cancelSession()
                        navController.popBackStack()
                    },
                    // Sesli yönlendirme kontrolleri
                    onRepeatInstruction = {
                        viewModel.repeatInstruction()
                    },
                    onGiveMotivation = {
                        viewModel.giveMotivation()
                    },
                    onStopVoice = {
                        viewModel.stopVoiceGuidance()
                    }
                )
            }
            
            // Ağrı Günlüğü Ekranı
            composable("${NavigationRoutes.PAIN_DIARY}/{sessionId}/{sessionName}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                val sessionName = backStackEntry.arguments?.getString("sessionName") ?: ""
                
                PainDiaryScreen(
                    sessionId = sessionId,
                    sessionName = sessionName,
                    onPainSubmitted = { painEntry ->
                        viewModel.addPainEntry(painEntry)
                        navController.navigate(NavigationRoutes.MAIN_HOME) {
                            popUpTo(NavigationRoutes.MAIN_HOME) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(NavigationRoutes.MAIN_HOME) {
                            popUpTo(NavigationRoutes.MAIN_HOME) { inclusive = true }
                        }
                    }
                )
            }
            
            // Analiz Ekranı
            composable(NavigationRoutes.ANALYTICS) {
                AnalyticsScreen(
                    user = user,
                    dailyProgress = viewModel.getDailyProgress(),
                    completedSessions = viewModel.completedSessions,
                    painEntries = viewModel.painEntries,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Ağrı Raporları Ekranı
            composable(NavigationRoutes.PAIN_REPORTS) {
                PainReportsScreen(
                    painEntries = viewModel.painEntries,
                    completedSessions = viewModel.completedSessions,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Sesli Yönlendirme Ayarları Ekranı
            composable(NavigationRoutes.VOICE_SETTINGS) {
                VoiceSettingsScreen(
                    currentSettings = viewModel.voiceSettings.value,
                    onSettingsChange = { settings ->
                        viewModel.updateVoiceSettings(settings)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onTestVoice = {
                        viewModel.giveMotivation()
                    }
                )
            }
            
            // Ağrı Geçmişi Ekranı
            composable(NavigationRoutes.PAIN_HISTORY) {
                PainHistoryScreen(
                    painEntries = viewModel.painEntries,
                    completedSessions = viewModel.completedSessions,
                    onEditPainEntry = { updatedEntry ->
                        viewModel.updatePainEntry(updatedEntry)
                    },
                    onDeletePainEntry = { entryId ->
                        viewModel.deletePainEntry(entryId)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Kullanıcı Profilleme Anketi
            composable(NavigationRoutes.USER_PROFILING) {
                UserProfilingScreen(
                    onProfilingComplete = { userProfile ->
                        viewModel.setUserProfile(userProfile)
                        navController.navigate(NavigationRoutes.AI_RECOMMENDATION) {
                            popUpTo(NavigationRoutes.USER_PROFILING) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(NavigationRoutes.MAIN_HOME) {
                            popUpTo(NavigationRoutes.USER_PROFILING) { inclusive = true }
                        }
                    }
                )
            }
            
            // AI Akıllı Öneri Ekranı
            composable(NavigationRoutes.AI_RECOMMENDATION) {
                val userProfile = viewModel.userProfile.value
                if (userProfile != null) {
                    AIRecommendationScreen(
                        userProfile = userProfile,
                        currentPainLevel = viewModel.painEntries.lastOrNull()?.painLevel,
                        previousSessions = viewModel.completedSessions.takeLast(5).map { it.templateName },
                        onRecommendationAccepted = { sessionTemplate ->
                            viewModel.addSessionTemplate(sessionTemplate)
                            navController.navigate(NavigationRoutes.MAIN_HOME) {
                                popUpTo(NavigationRoutes.AI_RECOMMENDATION) { inclusive = true }
                            }
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onRegenerateRequest = {
                            viewModel.generateAIRecommendations()
                        }
                    )
                } else {
                    // Profil yoksa anket ekranına yönlendir
                    LaunchedEffect(Unit) {
                        navController.navigate(NavigationRoutes.USER_PROFILING)
                    }
                }
            }
        }
    }
}