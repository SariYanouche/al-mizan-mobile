package com.klodit.almizan.data.repository

import com.klodit.almizan.data.api.CreateRecoursRequest
import com.klodit.almizan.data.api.RecoursApiService
import com.klodit.almizan.data.remote.ApiClient

class RecoursRepository {
    private val api = ApiClient.retrofit.create(RecoursApiService::class.java)
    private val profileRepository = ProfileRepository()

    suspend fun getMesRecours() = api.getRecoursOperateur(profileRepository.getCurrentOperateurId())
    
    suspend fun getRecoursDetail(id: String) = api.getRecoursById(id)

    suspend fun submitAppeal(aoId: String, attributionId: String, motif: String): Result<String> {
        return try {
            val operateurId = profileRepository.getCurrentOperateurId()
            val request = CreateRecoursRequest(
                appelOffreId = aoId,
                operateurId = operateurId,
                attributionProvisoireId = attributionId,
                motif = motif,
                piecesJointesUrls = emptyList() // Uploading files to MinIO can be added in polish phase
            )
            
            val response = api.createRecours(request)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.id)
            } else {
                Result.failure(Exception("Erreur de soumission du recours. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}