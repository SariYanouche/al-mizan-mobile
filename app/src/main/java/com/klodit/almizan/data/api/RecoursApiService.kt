package com.klodit.almizan.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class CreateRecoursRequest(
    val appelOffreId: String,
    val operateurId: String,
    val attributionProvisoireId: String,
    val motif: String,
    val piecesJointesUrls: List<String> = emptyList()
)

data class RecoursRecord(
    val id: String,
    val reference: String?,
    val appelOffreId: String?,
    val statut: String?,
    val motif: String?,
    val dateDepot: String?,
    val dateLimiteReponse: String?,
    val appelOffre: AppelOffreRef?,
    val attribution: AttributionRef?,
    val decision: DecisionRef?
)

data class AppelOffreRef(val id: String?, val reference: String?, val objet: String?)
data class AttributionRef(val winner: String?, val montantAttribue: String?, val dateAttribution: String?)
data class DecisionRef(val statut: String?, val motif: String?, val date: String?)

interface RecoursApiService {
    // 1. List all appeals for the logged-in operator
    @GET("recours/operateur/{operateurId}")
    suspend fun getRecoursOperateur(@Path("operateurId") operateurId: String): Response<ApiResponse<List<RecoursRecord>>>

    // 2. Get details of a specific appeal
    @GET("recours/{id}")
    suspend fun getRecoursById(@Path("id") id: String): Response<ApiResponse<RecoursRecord>>

    // 3. Submit a new appeal
    @POST("recours")
    suspend fun createRecours(@Body request: CreateRecoursRequest): Response<ApiResponse<RecoursRecord>>
}