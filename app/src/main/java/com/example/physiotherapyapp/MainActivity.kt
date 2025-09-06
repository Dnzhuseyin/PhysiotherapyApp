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
import com.example.physiotherapyapp.viewmodel.AuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth

/**
 * Ana Activity - Fizik Tedavi Uygulaması
 * 
 * Bu uygulama kullanıcıların fizik tedavi egzersizlerini seans mantığıyla 
 * takip edebilmesini sağlar. Tüm veriler geçici bellekte (ViewModel) tutulur.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Firebase'i manuel olarak initialize et
        try {
            // Önce mevcut Firebase instance'ı kontrol et
            val existingApp = try {
                FirebaseApp.getInstance()
            } catch (e: IllegalStateException) {
                null
            }
            
            if (existingApp == null) {
                // Manuel olarak Firebase options ile initialize et
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:1039715312277:android:f1bbd245585309ed03b19c")
                    .setApiKey("AIzaSyCdQ-Sm50M6WPTmej8YAa7lE95WzJV2q-c")
                    .setProjectId("fizikteadavi")
                    .setStorageBucket("fizikteadavi.firebasestorage.app")
                    .build()
                
                FirebaseApp.initializeApp(this, options)
                android.util.Log.d("MainActivity", "Firebase manually initialized with options")
            } else {
                android.util.Log.d("MainActivity", "Firebase already initialized: ${existingApp.name}")
            }
            
            // Auth instance'ı test et
            val auth = FirebaseAuth.getInstance()
            android.util.Log.d("MainActivity", "Firebase Auth instance created: ${auth.app.name}")
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Firebase initialization failed", e)
            
            // Fallback: Default initialize
            try {
                FirebaseApp.initializeApp(this)
                android.util.Log.d("MainActivity", "Firebase default initialized as fallback")
            } catch (fallbackException: Exception) {
                android.util.Log.e("MainActivity", "Firebase fallback initialization also failed", fallbackException)
            }
        }
        
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
    
    // ViewModels
    val viewModel: PhysiotherapyViewModel = viewModel {
        PhysiotherapyViewModel(context = context)
    }
    
    val authViewModel: AuthViewModel = viewModel()
    
    // Auth durumunu kontrol et
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    // Auth state değişikliklerini dinle
    LaunchedEffect(isLoggedIn) {
        android.util.Log.d("MainActivity", "Auth state changed: isLoggedIn=$isLoggedIn")
        if (isLoggedIn) {
            // Kullanıcı giriş yaptı, verileri yükle
            viewModel.loadDataFromFirebase()
        }
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
            startDestination = if (isLoggedIn) NavigationRoutes.MAIN_HOME else NavigationRoutes.AUTH,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Firebase Authentication Ekranı
            composable(NavigationRoutes.AUTH) {
                AuthScreen(
                    onLoginSuccess = {
                        navController.navigate(NavigationRoutes.MAIN_HOME) {
                            popUpTo(NavigationRoutes.AUTH) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(NavigationRoutes.MAIN_HOME) {
                            popUpTo(NavigationRoutes.AUTH) { inclusive = true }
                        }
                    },
                    onAuthError = { error ->
                        // Error handling yapılabilir
                    },
                    authViewModel = authViewModel
                )
            }
            
            // Eski Giriş Ekranı (Firebase olmadan)
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
                    completedSessions = completedSessions,
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
                    onBackClick = { /* Bottom nav kullanıldığı için geri buton yok */ },
                    onLogout = {
                        // Firebase'den çıkış yap
                        FirebaseAuth.getInstance().signOut()
                        // Auth ekranına yönlendir
                        navController.navigate(NavigationRoutes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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
                        // Voice guidance kaldırıldı
                    },
                    onGiveMotivation = {
                        // Voice guidance kaldırıldı
                    },
                    onStopVoice = {
                        // Voice guidance kaldırıldı
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
                    onPainSubmitted = { painLevel, bodyPart, notes ->
                        viewModel.addPainEntry(sessionId, painLevel, bodyPart, notes)
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
                // Voice guidance artık desteklenmiyor
                navController.popBackStack()
            }
            
            // Ağrı Geçmişi Ekranı
            composable(NavigationRoutes.PAIN_HISTORY) {
                PainHistoryScreen(
                    painEntries = viewModel.painEntries,
                    completedSessions = viewModel.completedSessions,
                    onEditPainEntry = { painEntry, newPainLevel, newNotes ->
                        viewModel.editPainEntry(painEntry, newPainLevel, newNotes)
                    },
                    onDeletePainEntry = { painEntry ->
                        viewModel.deletePainEntry(painEntry)
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
                        onRecommendationAccepted = { recommendation ->
                            viewModel.acceptAIRecommendation(recommendation)
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