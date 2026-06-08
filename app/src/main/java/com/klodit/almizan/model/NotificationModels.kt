package com.klodit.almizan.model

// ─── Enums ────────────────────────────────────────────────────────────────────

enum class NotificationType { EMAIL, SMS, PUSH, PLATEFORME }

enum class NotificationCategorie {
    PUBLICATION, DEPOT, OUVERTURE, EVALUATION, ATTRIBUTION,
    RECOURS, SYSTEME, IA_DIVERGENCE, IA_ERREUR
}

enum class NotificationStatut { EN_ATTENTE, ENVOYE, ECHEC, LU }

// ─── Response models ──────────────────────────────────────────────────────────

data class NotificationDto(
    val id           : String,
    val userId       : String,
    val titre        : String,
    val contenu      : String,
    val type         : String,
    val categorie    : String,
    val statut       : String,
    val isLue        : Boolean,
    val dateEnvoi    : String?,
    val dateLecture  : String?,
    val destinataire : String?,
    val refEntiteId  : String?,
    val refEntiteType: String?,
    val createdAt    : String
)

data class PaginatedNotificationsDto(
    val data       : List<NotificationDto>,
    val total      : Int,
    val page       : Int,
    val limit      : Int,
    val totalPages : Int
)

data class CountDto(val count: Int)