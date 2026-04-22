package com.klodit.almizan.model

import com.google.gson.annotations.SerializedName

/**
 * Aligned with Swagger: POST /auth/register and Tender Service
 */
enum class OrganisationType {
    EPA, EPIC, MINISTERE, ENTREPRISE_PRIVEE, ENTREPRISE_PUBLIQUE, GROUPEMENT
}

/**
 * Aligned with Swagger: Soumissions Service Statuses
 */
enum class SubmissionStatus {
    @SerializedName("brouillon") BROUILLON,
    @SerializedName("deposee") DEPOSEE,
    @SerializedName("recue") RECUE,
    @SerializedName("evaluee") EVALUEE,
    @SerializedName("retenue") RETENUE,
    @SerializedName("rejetee") REJETEE
}

data class Tender(
    val id: String,
    val reference: String,
    val objet: String,
    val typeProcedure: String,
    val statut: String,
    val dateLimiteSoumission: String,
    val wilaya: String,
    val montantEstime: Double? = null
)

data class Submission(
    val id: String,
    val reference: String,
    val appelOffreId: String,
    val statut: SubmissionStatus,
    val horodatageServeur: String? = null,
    val created_at: String
)