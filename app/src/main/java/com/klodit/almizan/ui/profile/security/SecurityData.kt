package com.klodit.almizan.ui.profile.security

import java.time.LocalDateTime

/**
 * Data models for the Security & Authentication screen.
 * Matches the DB schema: UserSecurity and Session tables.
 */

/**
 * Represents user security settings.
 * @param mfaEnabled Whether two-factor authentication is enabled.
 * @param lastLogin Timestamp of the user's last login.
 */
data class UserSecurity(
    val mfaEnabled: Boolean,
    val lastLogin: LocalDateTime
)

/**
 * Represents an active user session.
 * @param id Unique session identifier.
 * @param ipAddress IP address from which the session was created.
 * @param userAgent Device/browser information.
 * @param expiresAt When the session expires.
 * @param createdAt When the session was created.
 * @param isCurrentSession Local UI flag indicating if this is the current device's session.
 */
data class Session(
    val id: String,
    val ipAddress: String,
    val userAgent: String,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime,
    val isCurrentSession: Boolean = false
)


