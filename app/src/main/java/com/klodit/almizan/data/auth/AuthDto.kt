package com.klodit.almizan.data.auth

data class RegisterRequest(
    val email             : String,
    val password          : String,
    val role              : String,          // ← no default; pass explicitly
    val langue            : String = "fr",
    val nom               : String,
    val prenom            : String,
    val telephone         : String,
    val denomination      : String,
    val nif               : String,
    val nis               : String,
    val registre_commerce : String,
    val adresse           : String = "",
    val wilaya            : String = "",
    val commune           : String = "",
    val type              : String,          // ← no default; pass from Step 1
    // SERVICE_CONTRACTANT fields (nullable for OE)
    val code_service      : String? = null,
    val secteur_activite  : String? = null,
    val ordonnateur       : String? = null,
    // OPERATEUR_ECONOMIQUE fields (nullable for SC)
    val qualifications    : String? = null,
    val categories        : String? = null
)



data class RegisterResponse(
    val message : String? = null,
    val user_id : String? = null,
    val access_token : String? = null,
    val token        : String? = null
){
    fun resolvedToken() = access_token ?: token
}

data class LoginRequest(
    val email    : String,
    val password : String
)

data class LoginResponse(
    val message      : String? = null,
    val access_token : String? = null,
    val token        : String? = null,   // some APIs use "token" instead
    val user         : UserLogin? = null
) {
    // Whichever field the API actually returns
    fun resolvedToken() = access_token ?: token
}

data class UserLogin(
    val userId : String? = null,
    val email  : String? = null,
    val role   : String? = null
)

// ── Forgot password ───────────────────────────────────────────────────────────
data class ForgotPasswordRequest(
    val email: String
)

// ── Verify token (the 6-digit code sent by email) ────────────────────────────
data class VerifyTokenRequest(
    val token: String   // the 6-digit code the user receives
)

// ── Reset password ──────────────────────────────────────────────────────────────
data class ResetPasswordRequest(
    val token                : String,
    val newPassword          : String,
    val confirmeNewPassword  : String
)

// ── Generic response (message only) ──────────────────────────────────────────
data class MessageResponse(
    val message: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmeNewPassword: String
)

