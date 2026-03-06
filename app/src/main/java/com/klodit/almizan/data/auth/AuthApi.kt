package com.klodit.almizan.data.auth

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse


   @POST("auth/login")
   suspend fun loginRaw(
       @Body request: LoginRequest
   ): retrofit2.Response<LoginResponse>
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): MessageResponse

    @POST("auth/verify-token")
    suspend fun verifyToken(@Body request: VerifyTokenRequest): MessageResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): MessageResponse

    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): ResponseBody


    @POST("auth/logout")
    suspend fun logout(): MessageResponse

    @POST("auth/otp/send")          // ← was "otp/send"
    suspend fun sendOtp(@Body request: SendOtpRequest): OtpResponse

    @POST("auth/otp/verify")        // ← was "otp/verify"
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): OtpResponse


    @POST("auth/refresh")
    suspend fun refresh(): retrofit2.Response<MessageResponse>


}