package com.klodit.almizan.viewmodel.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.auth.AuthApi
import com.klodit.almizan.data.auth.AuthRepository
import com.klodit.almizan.data.auth.RegisterRequest
import com.klodit.almizan.data.remote.ApiClient
import com.klodit.almizan.ui.theme.AppLanguage
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import com.klodit.almizan.data.remote.TokenStorage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class RegStep1Data(
    val orgName : String,
    val nif     : String,
    val nis     : String,
    val rc      : String,
    val type    : String,
    val role    : String,
    val wilaya  : String,   //
    val commune : String,   //
    val adresse : String    //
)

data class RegStep2Data(
    val phone    : String,
    val email    : String,
    val password : String,
    val nom      : String,
    val prenom   : String
)

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val message: String = "") : AuthState()
    data class Error(val message: String)        : AuthState()
}

class AuthViewModel : ViewModel() {

    private val api        = ApiClient.retrofit.create(AuthApi::class.java)
    private val repository = AuthRepository(api)

    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var uploadState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var step1Data: RegStep1Data? by mutableStateOf(null)
        private set

    var step2Data: RegStep2Data? by mutableStateOf(null)
        private set

    var authToken by mutableStateOf<String?>(null)
        private set

    var currentUserId by mutableStateOf<String?>(null)
        private set

    private var registeredEmail: String = ""
    fun getRegisteredEmail() = registeredEmail

    var failedLoginAttempts by mutableStateOf(0)
        private set

    // ── Login ─────────────────────────────────────────────────────────────────
    fun login(
        email      : String,
        password   : String,
        rememberMe : Boolean = false,
        context    : Context? = null,
        onSuccess  : (token: String, userId: String) -> Unit,
        onLocked   : () -> Unit = {}
    ) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val (response, token, refreshToken) = repository.login(email, password)
                failedLoginAttempts = 0
                authToken     = token
                currentUserId = decodeUserIdFromJwt(token) ?: response.user?.userId
                authState     = AuthState.Success(token)
                if (rememberMe && context != null) {
                    TokenStorage.save(context, token, currentUserId ?: "", refreshToken)
                }
                onSuccess(token, currentUserId ?: "")
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                when (e.code()) {
                    401 -> {
                        failedLoginAttempts++
                        if (failedLoginAttempts >= 5) {
                            authState = AuthState.Idle
                            onLocked()
                        } else {
                            authState = AuthState.Error(
                                "Email ou mot de passe incorrect (${failedLoginAttempts}/5)"
                            )
                        }
                    }
                    403  -> authState = AuthState.Error("Accès refusé")
                    429  -> { authState = AuthState.Idle; onLocked() }
                    502  -> authState = AuthState.Error("Service temporairement indisponible")
                    else -> authState = AuthState.Error("Erreur serveur (${e.code()}): $errorBody")
                }
            } catch (e: java.net.UnknownHostException) {
                authState = AuthState.Error("Pas de connexion internet")
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Erreur de connexion")
            }
        }
    }

    fun resetFailedAttempts() { failedLoginAttempts = 0 }

    // ── Registration step helpers ─────────────────────────────────────────────
    fun saveStep1(
        orgName : String,
        nif     : String,
        nis     : String,
        rc      : String,
        type    : String,
        role    : String,
        wilaya  : String,   // ← NEW
        commune : String,   // ← NEW
        adresse : String    // ← NEW
    ) {
        step1Data = RegStep1Data(orgName, nif, nis, rc, type, role, wilaya, commune, adresse)
    }

    fun saveStep2(phone: String, email: String, password: String, nom: String, prenom: String) {
        step2Data = RegStep2Data(phone, email, password, nom, prenom)
    }

    // ── Final registration submit ─────────────────────────────────────────────
    fun register(
        selectedLang: AppLanguage = AppLanguage.FRENCH,
        onSuccess   : (userId: String) -> Unit
    ) {
        val s1 = step1Data ?: run { authState = AuthState.Error("Données étape 1 manquantes"); return }
        val s2 = step2Data ?: run { authState = AuthState.Error("Données étape 2 manquantes"); return }

        val request = RegisterRequest(
            email             = s2.email,
            password          = s2.password,
            role              = s1.role,           // ← from step1 selection
            //langue            = selectedLang.locale,
            langue  = when (selectedLang) {
                AppLanguage.ARABIC  -> "ar"
                else                -> "fr"
            },
            nom               = s2.nom,
            prenom            = s2.prenom,
            telephone         = s2.phone,
            denomination      = s1.orgName,
            nif               = s1.nif,
            nis               = s1.nis,
            registre_commerce = s1.rc,
            adresse           = s1.adresse,                // ← empty, not "string"
            wilaya            = s1.wilaya,
            commune           = s1.commune,
            type              = s1.type,           // ← from step1 selection
            // SC-specific fields — only send when role matches
            code_service     = if (s1.role == "SERVICE_CONTRACTANT") "SC-000" else null,
            secteur_activite = if (s1.role == "SERVICE_CONTRACTANT") "Non renseigné" else null,
            ordonnateur      = if (s1.role == "SERVICE_CONTRACTANT") "Non renseigné" else null,
            // OE-specific fields
            qualifications   = if (s1.role == "OPERATEUR_ECONOMIQUE") "" else null,
            categories       = if (s1.role == "OPERATEUR_ECONOMIQUE") "" else null
        )

        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                android.util.Log.d("REGISTER_DEBUG", com.google.gson.Gson().toJson(request))
                val response    = repository.register(request)
                registeredEmail = s2.email
                authToken       = response.resolvedToken()
                currentUserId   = response.user_id ?: ""
                authState       = AuthState.Success(response.message ?: "Inscription réussie")
                onSuccess(response.user_id ?: "")
                android.util.Log.d("REGISTER_DEBUG", com.google.gson.Gson().toJson(request))
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("AUTH_DEBUG", "HTTP ${e.code()}: $errorBody")
                authState = AuthState.Error(
                    when (e.code()) {
                        400  -> "Champs manquants ou invalides"   // ← added 400 case
                        409  -> "Un compte avec cet email existe déjà"
                        422  -> "Données invalides: $errorBody"
                        429  -> "Trop de tentatives, réessayez plus tard"
                        502  -> "Service temporairement indisponible"
                        else -> "Erreur serveur (${e.code()})"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                authState = AuthState.Error("Pas de connexion internet")
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Erreur d'inscription")
            }
        }
    }

    // ── Send OTP ──────────────────────────────────────────────────────────────
    fun sendOtp(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val res = repository.sendOtp(email)
                if (res.success == true) {
                    authState = AuthState.Idle
                    onSuccess()
                } else {
                    authState = AuthState.Error(res.message ?: "Échec d'envoi du code OTP")
                }
            } catch (e: retrofit2.HttpException) {
                authState = AuthState.Error(
                    when (e.code()) {
                        400  -> "Email invalide"
                        404  -> "Email introuvable"
                        429  -> "Trop de tentatives, réessayez plus tard"
                        500  -> "Erreur serveur lors de l'envoi"
                        502  -> "Service temporairement indisponible"
                        else -> "Erreur serveur (${e.code()})"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                authState = AuthState.Error("Pas de connexion internet")
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Erreur réseau")
            }
        }
    }

    // ── Verify OTP ────────────────────────────────────────────────────────────
    fun verifyOtp(email: String, code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val res = repository.verifyOtp(email, code)
                if (res.success == true) {
                    authState = AuthState.Idle
                    onSuccess()
                } else {
                    authState = AuthState.Error(res.message ?: "Code OTP invalide")
                }
            } catch (e: retrofit2.HttpException) {
                authState = AuthState.Error(
                    when (e.code()) {
                        400  -> "Code incorrect, expiré ou déjà utilisé"  // ← matches spec
                        401  -> "Code invalide ou expiré"
                        429  -> "Trop de tentatives"
                        else -> "Erreur serveur (${e.code()})"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                authState = AuthState.Error("Pas de connexion internet")
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Erreur")
            }
        }
    }

    // ── Forgot password ───────────────────────────────────────────────────────
    fun forgotPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                repository.forgotPassword(email)
                authState = AuthState.Success("Code envoyé à $email")
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                authState = AuthState.Error(
                    when (e.code()) {
                        400  -> "Format d'email invalide"   // ← matches spec
                        404  -> "Aucun compte trouvé avec cet email"
                        429  -> "Trop de tentatives, réessayez plus tard"
                        else -> "Erreur serveur (${e.code()})"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                authState = AuthState.Error("Pas de connexion internet")
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Erreur")
            }
        }
    }

    // ── Verify forgot-password token ──────────────────────────────────────────
    fun verifyToken(token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                repository.verifyToken(token)
                authState = AuthState.Success()
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                authState = AuthState.Error(
                    when (e.code()) {
                        400  -> "Code invalide ou expiré"   // ← spec returns 400, not 401
                        429  -> "Trop de tentatives"
                        else -> "Erreur serveur (${e.code()})"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                authState = AuthState.Error("Pas de connexion internet")
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Erreur")
            }
        }
    }

    // ── Reset password ────────────────────────────────────────────────────────
    fun resetPassword(token: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                repository.resetPassword(token, newPassword)
                authState = AuthState.Success("Mot de passe réinitialisé")
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                authState = AuthState.Error(
                    when (e.code()) {
                        400  -> "Code invalide ou critères non respectés"  // ← spec returns 400
                        429  -> "Trop de tentatives"
                        else -> "Erreur serveur (${e.code()})"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                authState = AuthState.Error("Pas de connexion internet")
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Erreur")
            }
        }
    }

    // ── Document upload ───────────────────────────────────────────────────────
    fun uploadDocument(context: Context, uri: Uri, onSuccess: () -> Unit) {
        val token = authToken ?: run { uploadState = AuthState.Error("Non authentifié"); return }
        viewModelScope.launch {
            uploadState = AuthState.Loading
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes       = inputStream?.readBytes() ?: throw Exception("Fichier illisible")
                val mimeType    = context.contentResolver.getType(uri) ?: "application/pdf"
                val fileName    = uri.lastPathSegment ?: "document.pdf"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part        = MultipartBody.Part.createFormData("file", fileName, requestBody)
                repository.uploadDocument(token, part)
                uploadState = AuthState.Success("Document uploadé")
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                uploadState = AuthState.Error(
                    when (e.code()) {
                        401  -> "Session expirée, reconnectez-vous"
                        413  -> "Fichier trop volumineux"
                        415  -> "Format de fichier non supporté"
                        429  -> "Trop de tentatives"
                        502  -> "Service indisponible"
                        else -> "Erreur upload (${e.code()})"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                uploadState = AuthState.Error("Pas de connexion internet")
            } catch (e: Exception) {
                uploadState = AuthState.Error(e.message ?: "Erreur upload")
            }
        }
    }

    // ── State helpers ─────────────────────────────────────────────────────────
    fun clearError()       { if (authState is AuthState.Error) authState = AuthState.Idle }
    fun clearUploadError() { if (uploadState is AuthState.Error) uploadState = AuthState.Idle }

    fun resetState() {
        authState = AuthState.Idle
        step1Data = null
        step2Data = null
    }

    fun restoreSession(token: String, userId: String) {
        authToken     = token
        currentUserId = userId
        authState     = AuthState.Success(token)
    }

    fun clearSession(context: Context? = null) {
        context?.let { TokenStorage.clear(it) }
        authToken           = null
        currentUserId       = null
        authState           = AuthState.Idle
        step1Data           = null
        step2Data           = null
        registeredEmail     = ""
        failedLoginAttempts = 0
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try { repository.logout() } catch (e: Exception) { /* ignore, clear anyway */ }
            clearSession()
            onSuccess()
        }
    }

    // ── JWT userId decoder ────────────────────────────────────────────────────
    private fun decodeUserIdFromJwt(token: String): String? {
        return try {
            val payload = token.split(".").getOrNull(1) ?: return null
            val padded  = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decoded = android.util.Base64.decode(padded, android.util.Base64.URL_SAFE)
            val json    = org.json.JSONObject(String(decoded))
            json.optString("sub").takeIf { it.isNotEmpty() }
                ?: json.optString("userId").takeIf { it.isNotEmpty() }
                ?: json.optString("id").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    fun verifyOtpAndLogin(
        email    : String,
        code     : String,
        onSuccess: (token: String, userId: String) -> Unit
    ) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                // Step 1: verify OTP
                val res = repository.verifyOtp(email, code)
                if (res.success != true) {
                    authState = AuthState.Error(res.message ?: "Code OTP invalide")
                    return@launch
                }
                // Step 2: auto-login with saved credentials
                val password = step2Data?.password ?: run {
                    authState = AuthState.Error("Session expirée, reconnectez-vous")
                    return@launch
                }
                //val (loginResponse, token) = repository.login(email, password)
                val (loginResponse, token, _) = repository.login(email, password)
                authToken     = token
                currentUserId = decodeUserIdFromJwt(token) ?: loginResponse.user?.userId
                authState     = AuthState.Success(token)
                onSuccess(token, currentUserId ?: "")
            } catch (e: retrofit2.HttpException) {
                authState = AuthState.Error(
                    when (e.code()) {
                        400  -> "Code incorrect, expiré ou déjà utilisé"
                        401  -> "Identifiants invalides"
                        else -> "Erreur serveur (${e.code()})"
                    }
                )
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Erreur")
            }
        }


    }

    fun tryRestoreSession(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            try {
                val savedUserId       = TokenStorage.getUserId(context) ?: run {
                    android.util.Log.e("RESTORE", "no userId saved")
                    onFailure(); return@launch
                }
                val savedRefreshToken = TokenStorage.getRefreshToken(context) ?: run {
                    android.util.Log.e("RESTORE", "no refresh token saved")
                    onFailure(); return@launch
                }

                android.util.Log.d("RESTORE", "userId=$savedUserId")
                android.util.Log.d("RESTORE", "refreshToken=${savedRefreshToken.take(20)}")

                ApiClient.injectRefreshToken(savedRefreshToken)

                val newToken = repository.refreshToken()
                android.util.Log.d("RESTORE", "newToken=${newToken.take(20)}")

                if (newToken.isNotBlank()) {
                    ApiClient.injectSavedToken(newToken)
                    TokenStorage.save(context, newToken, savedUserId, savedRefreshToken)
                    authToken     = newToken
                    currentUserId = savedUserId
                    authState     = AuthState.Success(newToken)
                    onSuccess()
                } else {
                    android.util.Log.e("RESTORE", "newToken is blank")
                    TokenStorage.clear(context)
                    onFailure()
                }
            } catch (e: Exception) {
                android.util.Log.e("RESTORE", "exception: ${e::class.simpleName} — ${e.message}")
                TokenStorage.clear(context)
                onFailure()
            }
        }
    }


    }