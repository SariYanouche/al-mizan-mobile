package com.klodit.almizan.ui.profile

import java.time.LocalDate
import java.time.LocalDateTime

// ─────────────────────────────────────────────
//  DATABASE MODELS
// ─────────────────────────────────────────────

enum class DocumentType {
    NIF,        // Numéro d'Identification Fiscale
    NIS,        // Numéro d'Identification Statistique
    RC,         // Registre de Commerce
    CNAS,       // Caisse Nationale des Assurances Sociales
    CASNOS,     // Caisse d'Assurance Sociale des Non-Salariés
    BILAN       // Bilan Comptable
}

data class PieceAdministrative(
    val id: Long,
    val type: DocumentType,
    val isValide: Boolean,
    val dateExpiration: LocalDate
)

data class Document(
    val nom: String,
    val tailleOctets: Long,
    val hashSha256: String,
    val createdAt: LocalDateTime
)

data class OcrAnalyse(
    val typeAnalyse: String,
    val scoreConfiance: Double,
    val isConforme: Boolean,
    val anomalies: String?
)

// ─────────────────────────────────────────────
//  UI MODEL
// ─────────────────────────────────────────────

data class DocumentUiModel(
    val id: Long,
    val type: DocumentType,
    val fileName: String,
    val fileSizeBytes: Long,
    val dateExpiration: LocalDate,
    val createdAt: LocalDateTime,
    val isValide: Boolean,
    val ocrScoreConfiance: Double,
    val ocrIsConforme: Boolean,
    val ocrAnomalies: String?
) {
    val isExpired: Boolean
        get() = dateExpiration.isBefore(LocalDate.now())

    val hasAiFlag: Boolean
        get() = !ocrIsConforme || !ocrAnomalies.isNullOrBlank()

    val formattedFileSize: String
        get() {
            return when {
                fileSizeBytes < 1024 -> "$fileSizeBytes B"
                fileSizeBytes < 1024 * 1024 -> "${fileSizeBytes / 1024} KB"
                else -> String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0))
            }
        }
}

// ─────────────────────────────────────────────
//  DOCUMENT STATUS
// ─────────────────────────────────────────────

enum class DocumentStatus {
    VALID,
    EXPIRED,
    AI_FLAG
}

fun DocumentUiModel.getStatus(): DocumentStatus {
    return when {
        isExpired -> DocumentStatus.EXPIRED
        hasAiFlag -> DocumentStatus.AI_FLAG
        isValide -> DocumentStatus.VALID
        else -> DocumentStatus.AI_FLAG
    }
}


