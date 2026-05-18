package com.klodit.almizan.model

import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
//  BID WIZARD DATA MODELS (Aligned with Next.js Web Client)
// ─────────────────────────────────────────────────────────────────────────────

data class Lot(
    val id: String,
    val appelOffreId: String,
    val numero: Int,
    val designation: String,
    val description: String? = null,
    val montantEstime: String? = null
)

data class UploadedDocument(
    val id: String = UUID.randomUUID().toString(),
    val nom: String,
    val typeMime: String = "application/pdf",
    val tailleOctets: Long,
    val fichierUrl: String,
    val hashSha256: String
)


data class OffreTechnique(
    val document: UploadedDocument? = null
)

data class BpuLine(
    val id: String = UUID.randomUUID().toString(),
    val designation: String = "",
    val unite: String = "U",
    val quantite: String = "1",
    val prixUnitaire: String = ""
)

data class LotBpu(
    val lotId: String,
    val lines: List<BpuLine> = emptyList()
)

data class CautionData(
    val montant: String = "",
    val banque: String = "",
    val reference: String = "",
    val dateEmission: String = "",
    val dateExpiration: String = "",
    val document: UploadedDocument? = null
)

data class AoOption(
    val id: String,
    val reference: String,
    val objet: String,
    val type: String,
    val status: String,
    val organisationName: String,
    val wilaya: String,
    val deadline: String,
    val lots: List<Lot>
)

data class BidWizardState(
    val availableAos: List<AoOption> = emptyList(),
    val selectedAoId: String? = null,
    val appelOffreId: String = "",
    val appelOffreReference: String = "",
    val appelOffreObjet: String = "",
    val currentStep: Int = 1,
    val totalSteps: Int = 5,

    // Step 1: Lot Selection
    val availableLots: List<Lot> = emptyList(),
    val selectedLotId: String? = null,


    // Step 2: Technical Offer
    val offreTechnique: OffreTechnique? = null,

    // Step 3: Financial Offer (BPU)
    val lotBpus: List<LotBpu> = emptyList(),

    // Step 4: Bank Guarantee
    val caution: CautionData = CautionData(),

    // Step 5: Final Review
    val certificationAccepted: Boolean = false
) {
    val progressPercent: Int
        get() = ((currentStep - 1) * 100) / (totalSteps - 1)

    val selectedLot: Lot?
        get() = availableLots.find { it.id == selectedLotId }

    // Validation Rules
    val canProceedToStep2: Boolean get() = selectedLotId != null
    val canProceedToStep3: Boolean get() = offreTechnique?.document != null
    val canProceedToStep4: Boolean get() = lotBpus.any { bpu -> bpu.lines.isNotEmpty() && bpu.lines.all { it.prixUnitaire.isNotBlank() } }
    val canProceedToStep5: Boolean get() = caution.montant.isNotBlank() && caution.banque.isNotBlank() && caution.reference.isNotBlank() && caution.document != null
    val canSubmit: Boolean get() = certificationAccepted && selectedLotId != null
}

data class SubmissionResult(
    val transactionId: String,
    val tenderReference: String,
    val timestamp: String,
    val selectedLot: Lot?
)