package com.klodit.almizan.model

import androidx.compose.ui.graphics.Color
import com.klodit.almizan.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
//  MY BIDS DATA MODELS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Internal status mapping from API strings.
 * Mirrors: soumissions.statut column values.
 */
enum class SoumissionStatut {
    BROUILLON,
    DEPOSEE,
    RECUE,
    EVALUEE,
    RETENUE,
    REJETEE
}

/**
 * UI presentation status for purchased Cahier des Charges.
 */
enum class PurchasedCDCStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED
}

/**
 * UI presentation status for submitted bids.
 * Each entry carries its display label, text color, and background color.
 */
enum class SubmissionUIStatus(
    val label: String,
    val color: Color,
    val backgroundColor: Color
) {
    IN_EVALUATION("EN ÉVALUATION", Navy800, Navy50),
    NOT_RETAINED("NON RETENU", Red600, Red50),
    AWARDED("ATTRIBUÉ", Green600, Green50)
}

/**
 * UI model for a purchased Cahier des Charges (CDC).
 * Built from combining Tender (Appel d'Offre) data with submission status.
 */
data class PurchasedCDC(
    val appelOffreId: String,
    val soumissionId: String? = null,
    val reference: String,
    val objet: String,
    val dateLimiteDepot: String,
    val statutSoumission: SoumissionStatut? = null
) {
    val uiStatus: PurchasedCDCStatus
        get() = when (statutSoumission) {
            SoumissionStatut.DEPOSEE,
            SoumissionStatut.RECUE,
            SoumissionStatut.EVALUEE,
            SoumissionStatut.RETENUE,
            SoumissionStatut.REJETEE -> PurchasedCDCStatus.SUBMITTED
            SoumissionStatut.BROUILLON -> PurchasedCDCStatus.IN_PROGRESS
            null -> PurchasedCDCStatus.NOT_STARTED
        }
}

/**
 * UI model for a submitted bid.
 * Built from combining Soumission data with its parent Appel d'Offre.
 */
data class BidSubmission(
    val soumissionId: String,
    val appelOffreId: String,
    val reference: String,
    val objet: String,
    val dateDepot: String,
    val statut: SoumissionStatut
) {
    val uiStatus: SubmissionUIStatus
        get() = when (statut) {
            SoumissionStatut.DEPOSEE,
            SoumissionStatut.RECUE,
            SoumissionStatut.EVALUEE,
            SoumissionStatut.BROUILLON -> SubmissionUIStatus.IN_EVALUATION
            SoumissionStatut.REJETEE -> SubmissionUIStatus.NOT_RETAINED
            SoumissionStatut.RETENUE -> SubmissionUIStatus.AWARDED
        }
}
