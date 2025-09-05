package com.example.physiotherapyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication ViewModel
 */
class AuthViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    
    init {
        // Network error için emulator test
        try {
            auth.useEmulator("10.0.2.2", 9099)
            android.util.Log.d("AuthViewModel", "Firebase Auth Emulator enabled for testing")
        } catch (e: Exception) {
            android.util.Log.d("AuthViewModel", "Firebase Auth Emulator not available, using production")
        }
        
        // Kullanıcının giriş durumunu kontrol et
        checkAuthState()
        
        // Firebase Auth state listener ekle
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            android.util.Log.d("AuthViewModel", "Firebase auth state changed: user=${user?.uid}")
            checkAuthState()
        }
    }
    
    /**
     * Mevcut auth durumunu kontrol et
     */
    private fun checkAuthState() {
        val user = auth.currentUser
        _isLoggedIn.value = user != null
        _currentUser.value = user?.let {
            FirebaseUser(
                uid = it.uid,
                email = it.email ?: "",
                displayName = it.displayName ?: ""
            )
        }
        
        android.util.Log.d("AuthViewModel", "checkAuthState: user=${user?.uid}, isLoggedIn=${_isLoggedIn.value}")
    }
    
    /**
     * Auth state'i manuel olarak yenile
     */
    fun refreshAuthState() {
        checkAuthState()
    }
    
    /**
     * Kullanıcı girişi
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                val result = auth.signInWithEmailAndPassword(email, password).await()
                
                if (result.user != null) {
                    val user = result.user!!
                    _isLoggedIn.value = true
                    _currentUser.value = FirebaseUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: ""
                    )
                    
                    // Auth state'i tekrar kontrol et
                    checkAuthState()
                    
                    _authState.value = AuthState.Success("Giriş başarılı!")
                    
                    android.util.Log.d("AuthViewModel", "Login success: uid=${user.uid}, isLoggedIn=${_isLoggedIn.value}")
                } else {
                    _authState.value = AuthState.Error("Giriş yapılamadı")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login error: ${e.message}", e)
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("CONFIGURATION_NOT_FOUND") == true -> 
                            "Firebase Console'da Authentication enable edilmemiş! Lütfen console'dan aktifleştirin."
                        e.message?.contains("network error") == true -> 
                            "İnternet bağlantısı yok! WiFi/Mobil veri kontrol edin."
                        e.message?.contains("timeout") == true -> 
                            "Bağlantı zaman aşımı! İnternet hızınızı kontrol edin."
                        e.message?.contains("There is no user record") == true -> 
                            "Bu email ile kayıtlı kullanıcı yok. Önce kayıt olun."
                        e.message?.contains("password is invalid") == true -> 
                            "Şifre hatalı"
                        e.message?.contains("email address is badly formatted") == true -> 
                            "Email formatı geçersiz"
                        e.message?.contains("too-many-requests") == true -> 
                            "Çok fazla deneme. Lütfen bekleyin"
                        else -> "Giriş hatası: ${e.message}"
                    }
                )
            }
        }
    }
    
    /**
     * Kullanıcı kaydı
     */
    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                // Firebase Authentication ile kayıt
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                if (result.user != null) {
                    val user = result.user!!
                    
                    // Kullanıcı profil bilgilerini güncelle
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    
                    user.updateProfile(profileUpdates).await()
                    
                    // Firestore'da kullanıcı dokümanı oluştur
                    createUserDocument(user.uid, email, displayName)
                    
                    // Auth state'i güncelle
                    _isLoggedIn.value = true
                    _currentUser.value = FirebaseUser(
                        uid = user.uid,
                        email = email,
                        displayName = displayName
                    )
                    
                    // Auth state'i tekrar kontrol et
                    checkAuthState()
                    
                    _authState.value = AuthState.Success("Hesap başarıyla oluşturuldu!")
                    
                    android.util.Log.d("AuthViewModel", "Register success: uid=${user.uid}, isLoggedIn=${_isLoggedIn.value}")
                    
                } else {
                    _authState.value = AuthState.Error("Hesap oluşturulamadı")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Register error: ${e.message}", e)
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("CONFIGURATION_NOT_FOUND") == true -> 
                            "Firebase Console'da Authentication enable edilmemiş! Lütfen console'dan aktifleştirin."
                        e.message?.contains("network error") == true -> 
                            "İnternet bağlantısı yok! WiFi/Mobil veri kontrol edin."
                        e.message?.contains("timeout") == true -> 
                            "Bağlantı zaman aşımı! İnternet hızınızı kontrol edin."
                        e.message?.contains("email-already-in-use") == true -> "Bu e-posta adresi zaten kullanımda"
                        e.message?.contains("weak-password") == true -> "Şifre çok zayıf (en az 6 karakter)"
                        e.message?.contains("invalid-email") == true -> "Geçersiz e-posta adresi"
                        else -> "Kayıt hatası: ${e.message}"
                    }
                )
            }
        }
    }
    
    /**
     * Firestore'da kullanıcı dokümanı oluştur
     */
    private suspend fun createUserDocument(uid: String, email: String, displayName: String) {
        try {
            val userDoc = hashMapOf(
                "uid" to uid,
                "email" to email,
                "displayName" to displayName,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "totalSessions" to 0,
                "totalPoints" to 0,
                "currentLevel" to 1,
                "badges" to emptyList<String>(),
                "profileCompleted" to false
            )
            
            firestore.collection("users")
                .document(uid)
                .set(userDoc)
                .await()
                
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Firestore kullanıcı oluşturma hatası", e)
        }
    }
    
    /**
     * Çıkış yap
     */
    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
    
    /**
     * Auth state'i sıfırla
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

/**
 * Authentication durumları
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Firebase kullanıcı veri modeli
 */
data class FirebaseUser(
    val uid: String,
    val email: String,
    val displayName: String
)
