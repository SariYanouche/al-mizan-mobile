package com.klodit.almizan.data.auth

data class SendOtpRequest(val email: String)

data class VerifyOtpRequest(val email: String, val code: String)

data class OtpResponse(
    val success: Boolean? = null,
    val message: String?  = null
)