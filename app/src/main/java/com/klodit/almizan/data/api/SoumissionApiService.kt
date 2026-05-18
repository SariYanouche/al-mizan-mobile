package com.klodit.almizan.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

// Generic API wrapper
data class ApiResponse<T>(
    val success: Boolean?,
    val data: T?,
    val message: String?
)

data class CreateDraftRequest(
    val appelOffreId: String,
    val lotId: String?
)

data class SoumissionResponseData(
    val id: String,
    val reference: String?,
    val statut: String?,
    val horodatageServeur: String?
)

data class SoumissionRecordDto(
    val id: String,
    val reference: String?,
    val appelOffreId: String?,
    val appel_offre_id: String?,
    val statut: String?,
    val status: String?,
    val horodatageServeur: String?,
    val horodatage_serveur: String?,
    val createdAt: String?,
    val created_at: String?
)

data class SoumissionDetailDto(
    val id: String,
    val reference: String?,
    val appelOffreId: String?,
    val appel_offre_id: String?,
    val statut: String?,
    val status: String?,
    val horodatageServeur: String?,
    val createdAt: String?,
    val offreTechnique: OffreTechniqueDto?,
    val offreFinanciere: OffreFinanciereDto?
)

data class OffreTechniqueDto(val hashFichier: String?, val isConforme: Boolean?)
data class OffreFinanciereDto(val montantTtc: Double?, val isDechiffree: Boolean?)

interface SoumissionApiService {
    
    @GET("soumissions?page=1&limit=500")
    suspend fun getMySubmissions(): Response<ApiResponse<List<SoumissionRecordDto>>>

    @GET("soumissions/{id}")
    suspend fun getSubmissionById(@Path("id") id: String): Response<ApiResponse<SoumissionDetailDto>>

    @POST("soumissions")
    suspend fun createDraft(@Body request: CreateDraftRequest): Response<ApiResponse<SoumissionResponseData>>

    @Multipart
    @POST("soumissions/{id}/offre-technique")
    suspend fun uploadTechOffer(
        @Path("id") id: String,
        @Part fichier: MultipartBody.Part,
        @Part("hashClient") hashClient: RequestBody?
    ): Response<ApiResponse<Any>>

    @Multipart
    @POST("soumissions/{id}/offre-financiere")
    suspend fun uploadFinOffer(
        @Path("id") id: String,
        @Part fichierChiffre: MultipartBody.Part,
        @Part("hashClient") hashClient: RequestBody?,
        @Part("signatureEcdsa") signatureEcdsa: RequestBody,
        @Part("clePubliqueEcdsaPem") clePubliqueEcdsaPem: RequestBody
    ): Response<ApiResponse<Any>>

    @Multipart
    @POST("soumissions/{id}/caution")
    suspend fun uploadCaution(
        @Path("id") id: String,
        @Part("donnees") donnees: RequestBody,
        @Part scanCaution: MultipartBody.Part
    ): Response<ApiResponse<Any>>

    @PUT("soumissions/{id}/valider")
    suspend fun validerSoumission(@Path("id") id: String): Response<ApiResponse<SoumissionResponseData>>
}