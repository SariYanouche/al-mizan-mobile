// app/src/main/java/com/klodit/almizan/data/repository/ProfileRepository.kt
package com.klodit.almizan.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.klodit.almizan.data.api.ProfileApiService
import com.klodit.almizan.data.auth.ChangePasswordRequest
import com.klodit.almizan.data.profile.ProfileApiResponse
import com.klodit.almizan.data.profile.ProfileResponse
import com.klodit.almizan.data.profile.UpdateProfileRequest
import com.klodit.almizan.data.remote.ApiClient
import com.klodit.almizan.ui.profile.*
import com.klodit.almizan.ui.profile.security.Session
import com.klodit.almizan.ui.profile.security.UserSecurity
import com.klodit.almizan.ui.profile.settings.AuditLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProfileRepository {
    private val api = ApiClient.retrofit.create(ProfileApiService::class.java)

    @Volatile
    private var cachedOperateurId: String? = null

    suspend fun getCurrentOperateurId(): String = withContext(Dispatchers.IO) {
        cachedOperateurId?.let { return@withContext it }

        val meRes = api.getMe()
        val userId = meRes.body()?.user?.userId
            ?: throw IllegalStateException("Failed to resolve userId from /auth/me")

        val opsRes = try { api.getOperateurs() } catch (e: Exception) {
            android.util.Log.e("PROFILE_REPO", "getOperateurs exception: $e")
            null }
        android.util.Log.d("PROFILE_REPO", "getOperateurs HTTP code = ${opsRes?.code()}")
        android.util.Log.d("PROFILE_REPO", "getOperateurs error body = ${opsRes?.errorBody()?.string()}")
        android.util.Log.d("PROFILE_REPO", "getOperateurs raw body = ${opsRes?.body()}")
        //val operateurs = opsRes?.body() ?: emptyList()
        val operateurs = opsRes?.body()?.data ?: emptyList()

       // val operateur = operateurs.find { it.userId == userId || it.user_id == userId }
        val operateur = operateurs.find { it.userId == userId }
            ?: operateurs.firstOrNull()

        val opId = operateur?.id ?: "fallback_operateur_id"
        cachedOperateurId = opId
        return@withContext opId
    }

    suspend fun getProfileScreenData(): Result<ProfileScreenData> = withContext(Dispatchers.IO) {
        try {
            val meRes = api.getMe()
            if (!meRes.isSuccessful) throw Exception("Auth failed")

            val userId = meRes.body()?.user?.userId ?: throw Exception("No user ID")
            val email = meRes.body()?.user?.email ?: "user@entreprise.dz"

            android.util.Log.d("PROFILE_REPO", "userId from /me = $userId")

            // Graceful fallback if profile or operator doesn't exist for test accounts
            val profileDto = try {
                val res = api.getProfile(userId)
                android.util.Log.d("PROFILE_REPO", "getProfile HTTP ${res.code()}")
                android.util.Log.d("PROFILE_REPO", "getProfile body = ${res.body()}")
                android.util.Log.d("PROFILE_REPO", "getProfile errorBody = ${res.errorBody()?.string()}")
                res.body()
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_REPO", "getProfile exception: $e")
                null
            }

            /*
            val operateurs = try {
                val res = api.getOperateurs()
                android.util.Log.d("PROFILE_REPO", "getOperateurs HTTP ${res.code()}")
                android.util.Log.d("PROFILE_REPO", "getOperateurs body = ${res.body()}")
                android.util.Log.d("PROFILE_REPO", "getOperateurs errorBody = ${res.errorBody()?.string()}")
                res.body() ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_REPO", "getOperateurs exception: $e")
                emptyList()
            }*/

            val operateurs = try {
                val res = api.getOperateurs()
                android.util.Log.d("PROFILE_REPO", "getOperateurs HTTP ${res.code()}")
                android.util.Log.d("PROFILE_REPO", "getOperateurs body = ${res.body()}")
                android.util.Log.d("PROFILE_REPO", "getOperateurs errorBody = ${res.errorBody()?.string()}")

                res.body()?.data ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_REPO", "getOperateurs exception: $e")
                emptyList()
            }


            android.util.Log.d("PROFILE_REPO", "operateurs count = ${operateurs.size}")
            operateurs.forEach {
                //android.util.Log.d("PROFILE_REPO", "  op.userId=${it.userId} op.user_id=${it.user_id} org=${it.organisation?.denomination}")
                android.util.Log.d(
                    "PROFILE_REPO",
                    "op.userId=${it.userId} org=${it.organisation?.denomination}"
                )
            }

           // val opDto = operateurs.find { it.userId == userId || it.user_id == userId } ?: operateurs.firstOrNull()

            val opDto = operateurs.find { it.userId == userId }
                ?: operateurs.firstOrNull()

            android.util.Log.d("PROFILE_REPO", "matched opDto = $opDto")
            val orgDto = opDto?.organisation

            opDto?.id?.let { cachedOperateurId = it }

            val profile = Profile(
                id = profileDto?.id ?: "",
                user_id = userId,
                nom = profileDto?.nom ?: "Nom non renseigné",
                prenom = profileDto?.prenom ?: "Prénom non renseigné",
                telephone = profileDto?.telephone ?: "-",
                langue = Langue.fromValue(profileDto?.langue ?: "fr")
            )

            val scProfile = try {
                api.getServiceContractantProfile(userId).body()
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_REPO", "getScProfile failed: $e")
                null
            }

            val organisation = Organisation(
                denomination      = scProfile?.organizationInfo?.denomination ?: orgDto?.denomination ?: "—",
                nif               = scProfile?.organizationInfo?.nif ?: orgDto?.nif ?: "—",
                nis               = scProfile?.organizationInfo?.nis ?: orgDto?.nis ?: "—",
                registre_commerce = scProfile?.organizationInfo?.rc ?: orgDto?.registre_commerce ?: "—",
                adresse           = scProfile?.organizationInfo?.address ?: orgDto?.adresse ?: "—",
                wilaya            = scProfile?.organizationInfo?.wilaya ?: orgDto?.wilaya ?: "—",
                commune           = orgDto?.commune ?: "—",
                type              = OrganisationType.fromValue(scProfile?.organizationInfo?.organizationType ?: orgDto?.type ?: ""),
                is_verified       = scProfile?.organizationInfo?.verificationStatus?.lowercase() == "verified" || orgDto?.is_verified ?: false
            )

            /*
            val operateur = OperateurEconomique(
                qualifications = opDto?.qualifications?.split(",")?.filter { it.isNotBlank() } ?: listOf("Standard"),
                categories = opDto?.categories?.split(",")?.filter { it.isNotBlank() } ?: listOf("Catégorie 1"),
                is_eligible = opDto?.is_eligible ?: true,
                is_blacklisted = opDto?.is_blacklisted ?: false,
                raison_blacklist = opDto?.raison_blacklist
            )*/
            val operateur = OperateurEconomique(
                qualifications = opDto?.qualifications
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?: listOf("Standard"),

                categories = opDto?.categories
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?: listOf("Catégorie 1"),

                is_eligible = opDto?.isEligible ?: true,
                is_blacklisted = opDto?.isBlacklisted ?: false,
                raison_blacklist = opDto?.raisonBlacklist
            )

            Result.success(ProfileScreenData(User(email, true), profile, organisation, operateur))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSessions(): Result<List<Session>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSessions()
            val sessions = response.body()?.mapIndexed { index, dto ->
                Session(
                    id = dto.id,
                    ipAddress = dto.ip_address ?: "Unknown IP",
                    userAgent = dto.user_agent ?: "Unknown Device",
                    expiresAt = parseIsoDate(dto.expires_at) ?: LocalDateTime.now().plusDays(1),
                    createdAt = parseIsoDate(dto.created_at) ?: LocalDateTime.now(),
                    isCurrentSession = index == 0
                )
            } ?: emptyList()
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getUserSecurity(): Result<UserSecurity> = withContext(Dispatchers.IO) {
        try {
            val sessions = getSessions().getOrDefault(emptyList())
            val lastLogin = sessions.maxByOrNull { it.createdAt }?.createdAt ?: LocalDateTime.now()
            Result.success(UserSecurity(mfaEnabled = false, lastLogin = lastLogin))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAuditLogs(): Result<List<AuditLog>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAuditLogs()
            val logs = response.body()?.map { dto ->
                AuditLog(
                    id = dto.id,
                    action = dto.action ?: "ACTION",
                    entite = dto.entite ?: "System",
                    ipAddress = dto.ip_address ?: "127.0.0.1",
                    horodatage = parseIsoDate(dto.horodatage) ?: LocalDateTime.now()
                )
            } ?: emptyList()
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getDocuments(): Result<List<DocumentUiModel>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDocuments()
            val documents = response.body()?.map { dto ->
                DocumentUiModel(
                    id = dto.id,
                    type = DocumentType.entries.find { it.name == dto.type?.uppercase() } ?: DocumentType.NIF,
                    fileName = dto.nom_fichier ?: "document.pdf",
                    fileSizeBytes = dto.taille_octets ?: 0L,
                    dateExpiration = parseLocalDate(dto.date_expiration) ?: LocalDate.now().plusYears(1),
                    createdAt = parseIsoDate(dto.created_at) ?: LocalDateTime.now(),
                    isValide = dto.is_valide ?: false,
                    ocrScoreConfiance = dto.ocr_score_confiance ?: 0.0,
                    ocrIsConforme = dto.ocr_is_conforme ?: false,
                    ocrAnomalies = dto.ocr_anomalies
                )
            } ?: emptyList()
            Result.success(documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        profileId: String,
        request: UpdateProfileRequest
    ): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProfile(profileId, request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Erreur mise a jour profil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

/*
    suspend fun deleteProfile(profileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProfile(profileId)
            android.util.Log.d("PROFILE_REPO", "deleteProfile HTTP ${response.code()}")
            android.util.Log.d("PROFILE_REPO", "deleteProfile body = ${response.body()}")
            android.util.Log.d("PROFILE_REPO", "deleteProfile errorBody = ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                // API returns { deleted: true } — body may be null on some 200s too
                val deleted = response.body()?.deleted ?: true
                if (deleted) Result.success(Unit)
                else Result.failure(Exception("Le serveur a refusé la suppression"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Erreur suppression compte (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("PROFILE_REPO", "deleteProfile exception: $e")
            Result.failure(e)
        }
    }*/

    suspend fun deleteProfile(profileId: String, token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProfile("Bearer $token", profileId)
            android.util.Log.d("DELETE_DEBUG", "HTTP ${response.code()}")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Erreur (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("DELETE_DEBUG", "exception: ${e::class.simpleName} — ${e.message}")
            Result.failure(Exception(e.message ?: "Erreur suppression"))
        }
    }

    private fun parseIsoDate(isoString: String?): LocalDateTime? = try {
        LocalDateTime.parse(isoString?.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (e: Exception) { null }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseLocalDate(dateString: String?): LocalDate? = try {
        LocalDate.parse(dateString?.substringBefore("T"), DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) { null }


    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmeNewPassword = newPassword
                )
            )
            android.util.Log.d("PROFILE_REPO", "changePassword HTTP ${response.code()}")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Erreur changement mot de passe (${response.code()})"
                // 401 = current password wrong
                val friendly = if (response.code() == 401)
                    "Mot de passe actuel incorrect"
                else errorMsg
                Result.failure(Exception(friendly))
            }
        } catch (e: Exception) {
            android.util.Log.e("PROFILE_REPO", "changePassword exception: $e")
            Result.failure(e)
        }
    }
}