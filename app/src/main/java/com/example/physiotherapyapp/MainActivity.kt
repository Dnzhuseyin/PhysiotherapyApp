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
    
    // Ana ViewModel
    val viewModel: PhysiotherapyViewModel = viewModel()
    
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
                        navController.navigate(NavigationRoutes.MAIN_HOME) {
                            // Login ekranını stack'ten kaldır
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
                    }
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
                        viewModel.completeSession()
                        navController.navigate(NavigationRoutes.MAIN_HOME) {
                            // Seans ekranını stack'ten kaldır
                            popUpTo(NavigationRoutes.MAIN_HOME) { inclusive = true }
                        }
                    },
                    onCancelSession = {
                        viewModel.cancelSession()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}