package com.klodit.almizan.ui.profile.settings

import java.time.LocalDateTime

// ─────────────────────────────────────────────
//  NOTIFICATION PREFERENCE DATA MODEL
// ─────────────────────────────────────────────

enum class NotificationChannel {
    EMAIL,
    SMS,
    PUSH
}

enum class NotificationCategory {
    PUBLICATION,
    EVALUATION,
    ATTRIBUTION,
    RECOURS,
    SYSTEME
}

data class NotificationPreference(
    val channels: Map<NotificationChannel, Boolean>,
    val categories: Map<NotificationCategory, Boolean>
)

// ─────────────────────────────────────────────
//  AUDIT LOG DATA MODEL
// ─────────────────────────────────────────────

data class AuditLog(
    val id: Long,
    val action: String,
    val entite: String,
    val ipAddress: String,
    val horodatage: LocalDateTime
)


