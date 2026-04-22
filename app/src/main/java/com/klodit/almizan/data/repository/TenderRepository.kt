package com.klodit.almizan.data.repository

import com.klodit.almizan.data.api.AttributionDto
import com.klodit.almizan.data.api.TenderApiService
import com.klodit.almizan.data.api.TenderDto
import com.klodit.almizan.data.remote.ApiClient

object TenderRepository {
    private val api = ApiClient.retrofit.create(TenderApiService::class.java)

    suspend fun getAvailableTenders(): Result<List<TenderDto>> {
        return try {
            val response = api.getAppelsOffres()
            if (response.isSuccessful && response.body()?.data != null) {
                val activeTenders = response.body()!!.data!!.filter {
                    it.statut?.uppercase() != "BROUILLON"
                }
                Result.success(activeTenders)
            } else {
                Result.failure(Exception("Erreur lors de la récupération des appels d'offres"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAttributions(): Result<List<AttributionDto>> {
        return try {
            val response = api.getAttributions()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erreur de récupération des attributions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTenderById(id: String): Result<TenderDto> {
        return try {
            val response = api.getTenderById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur lors de la récupération de l'appel d'offre (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}