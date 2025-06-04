package com.example.physiotherapyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
@Composable
fun PhysiotherapyApp() {
    // Navigation controller
    val navController = rememberNavController()
    
    // Ana ViewModel
    val viewModel: PhysiotherapyViewModel = viewModel()
    
    // ViewModel state'lerini observe et
    val user by viewModel.user
    val currentSession by viewModel.currentSession
    val currentExerciseIndex by viewModel.currentExerciseIndex
    val completedSessions = viewModel.completedSessions
    
    // Navigation Host - tüm ekranları yönetir
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.LOGIN
    ) {
        // Giriş Ekranı
        composable(NavigationRoutes.LOGIN) {
            LoginScreen(
                onContinueClick = {
                    navController.navigate(NavigationRoutes.HOME) {
                        // Login ekranını stack'ten kaldır
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        // Ana Ekran
        composable(NavigationRoutes.HOME) {
            HomeScreen(
                onExerciseSelectionClick = {
                    navController.navigate(NavigationRoutes.EXERCISE_SELECTION)
                },
                onSessionHistoryClick = {
                    navController.navigate(NavigationRoutes.SESSION_HISTORY)
                },
                onProfileClick = {
                    navController.navigate(NavigationRoutes.PROFILE)
                }
            )
        }
        
        // Egzersiz Seçimi Ekranı
        composable(NavigationRoutes.EXERCISE_SELECTION) {
            ExerciseSelectionScreen(
                availableExercises = viewModel.availableExercises,
                onBackClick = {
                    navController.popBackStack()
                },
                onStartSession = { selectedExercises ->
                    viewModel.startSession(selectedExercises)
                    navController.navigate(NavigationRoutes.SESSION) {
                        // Egzersiz seçimi ekranını stack'ten kaldır
                        popUpTo(NavigationRoutes.HOME)
                    }
                }
            )
        }
        
        // Aktif Seans Ekranı
        composable(NavigationRoutes.SESSION) {
            SessionScreen(
                session = currentSession,
                currentExerciseIndex = currentExerciseIndex,
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
                    navController.navigate(NavigationRoutes.HOME) {
                        // Seans ekranını stack'ten kaldır
                        popUpTo(NavigationRoutes.HOME) { inclusive = true }
                    }
                },
                onCancelSession = {
                    viewModel.cancelSession()
                    navController.navigate(NavigationRoutes.HOME) {
                        // Seans ekranını stack'ten kaldır
                        popUpTo(NavigationRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
        
        // Seans Geçmişi Ekranı
        composable(NavigationRoutes.SESSION_HISTORY) {
            SessionHistoryScreen(
                completedSessions = completedSessions,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Profil Ekranı
        composable(NavigationRoutes.PROFILE) {
            ProfileScreen(
                user = user,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}