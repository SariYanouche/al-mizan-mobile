package com.klodit.almizan.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.klodit.almizan.data.api.CreateDraftRequest
import com.klodit.almizan.data.api.SoumissionApiService
import com.klodit.almizan.data.remote.ApiClient
import com.klodit.almizan.model.BidWizardState
import com.klodit.almizan.model.SubmissionResult
import com.klodit.almizan.util.E2EECryptoManager
import com.klodit.almizan.util.FileUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class SoumissionRepository {

    private val api = ApiClient.retrofit.create(SoumissionApiService::class.java)
    private val gson = Gson()

    suspend fun submitBidWorkflow(state: BidWizardState, context: Context): Result<SubmissionResult> {
        try {
            val lotId = state.selectedLotId ?: throw Exception("Lot non sélectionné")

            // 1. Create Draft
            val draftRes = api.createDraft(CreateDraftRequest(state.appelOffreId, lotId))
            if (!draftRes.isSuccessful) throw Exception("Erreur création brouillon")
            val soumissionId = draftRes.body()?.data?.id ?: throw Exception("ID Soumission invalide")

            // 2. Upload Tech Offer
            val techUri = Uri.parse(state.offreTechnique?.document?.fichierUrl ?: throw Exception("Fichier technique manquant"))
            val techFile = FileUtil.uriToFile(context, techUri) ?: throw Exception("Impossible de lire le fichier tech")
            val techHash = E2EECryptoManager.computeSha256Hex(techFile)

            val techPart = FileUtil.createMultipartPart(techFile, "fichier")
            val techHashPart = techHash.toRequestBody("text/plain".toMediaTypeOrNull())

            val techRes = api.uploadTechOffer(soumissionId, techPart, techHashPart)
            if (!techRes.isSuccessful) throw Exception("Erreur upload offre technique")

            // 3. Encrypt & Upload Financial Offer
            // Convert BPU to JSON
            val financialJson = gson.toJson(mapOf(
                "format" to "bpu.v1",
                "lots" to state.lotBpus
            ))

            // Encrypt locally!
            val encryptedFinFile = E2EECryptoManager.encryptFinancialOffer(financialJson, context.cacheDir)
            val finHash = E2EECryptoManager.computeSha256Hex(encryptedFinFile)
            val ecdsaProof = E2EECryptoManager.generateEcdsaSignature(finHash)

            val finPart = FileUtil.createMultipartPart(encryptedFinFile, "fichierChiffre")
            val finHashPart = finHash.toRequestBody("text/plain".toMediaTypeOrNull())
            val sigPart = ecdsaProof.signatureBase64.toRequestBody("text/plain".toMediaTypeOrNull())
            val pubKeyPart = ecdsaProof.publicKeyPem.toRequestBody("text/plain".toMediaTypeOrNull())

            val finRes = api.uploadFinOffer(soumissionId, finPart, finHashPart, sigPart, pubKeyPart)
            if (!finRes.isSuccessful) throw Exception("Erreur upload offre financière chiffrée")

            // 4. Upload Caution
            val cautionUri = Uri.parse(state.caution.document?.fichierUrl ?: throw Exception("Fichier caution manquant"))
            val cautionFile = FileUtil.uriToFile(context, cautionUri) ?: throw Exception("Impossible de lire le fichier caution")
            val cautionPart = FileUtil.createMultipartPart(cautionFile, "scanCaution")

            val cautionJson = gson.toJson(mapOf(
                "montant" to (state.caution.montant.replace("\\s".toRegex(), "").replace(",", ".").toDoubleOrNull() ?: 0.0),
                "banque" to state.caution.banque,
                "reference" to state.caution.reference,
                "dateEmission" to "${toIsoDate(state.caution.dateEmission)}T00:00:00Z",
                "dateExpiration" to "${toIsoDate(state.caution.dateExpiration)}T23:59:59Z"
            ))
            val donneesPart = cautionJson.toRequestBody("application/json".toMediaTypeOrNull())

            val cautionRes = api.uploadCaution(soumissionId, donneesPart, cautionPart)
            if (!cautionRes.isSuccessful) throw Exception("Erreur upload caution")

            // 5. Final Validation
            val validerRes = api.validerSoumission(soumissionId)
            if (!validerRes.isSuccessful) throw Exception("Erreur lors de la validation finale")

            val finalData = validerRes.body()?.data ?: throw Exception("Données de validation manquantes")

            return Result.success(
                SubmissionResult(
                    transactionId = finalData.id,
                    tenderReference = state.appelOffreReference,
                    timestamp = finalData.horodatageServeur ?: "",
                    selectedLot = state.selectedLot
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }
    suspend fun getMySubmissions(): Result<List<com.klodit.almizan.data.api.SoumissionRecordDto>> {
        return try {
            val response = api.getMySubmissions()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erreur récupération soumissions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSubmissionById(id: String): Result<com.klodit.almizan.data.api.SoumissionDetailDto> {
        return try {
            val response = api.getSubmissionById(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erreur récupération détails soumission"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Converts a date string from DD/MM/YYYY (user input) to ISO 8601 YYYY-MM-DD.
     * If already in ISO format or unparseable, returns the input as-is.
     */
    private fun toIsoDate(input: String): String {
        val trimmed = input.trim()
        // Already ISO? (starts with 4-digit year)
        if (trimmed.matches(Regex("^\\d{4}-\\d{2}-\\d{2}.*"))) return trimmed.take(10)
        // Try DD/MM/YYYY
        val parts = trimmed.split("/")
        return if (parts.size == 3 && parts[0].length <= 2 && parts[1].length <= 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
        } else {
            trimmed // fallback: return as-is
        }
    }
}