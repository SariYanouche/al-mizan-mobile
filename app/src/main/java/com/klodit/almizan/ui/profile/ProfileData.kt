package com.klodit.almizan.ui.profile

/**
 * Data models for the Profile screen.
 * These models closely mirror the database schema for seamless API integration.
 */

// ─────────────────────────────────────────────
//  ENUMS
// ─────────────────────────────────────────────

enum class OrganisationType(val value: String) {
    EURL("eurl"),
    SARL("sarl"),
    SPA("spa"),
    SNC("snc"),
    ETABLISSEMENT("etablissement"),
    EPIC("epic"),
    EPA("epa");

    companion object {
        fun fromValue(value: String): OrganisationType =
            entries.find { it.value == value } ?: SARL
    }
}

enum class Langue(val value: String, val locale: String) {
    FR("fr", "fr"),
    AR("ar", "ar"),
    EN("en", "en");

    companion object {
        fun fromValue(value: String): Langue =
            entries.find { it.value == value } ?: FR
    }
}

// ─────────────────────────────────────────────
//  DATA CLASSES (mirror database schema)
// ─────────────────────────────────────────────

/**
 * Mirrors: users table
 */
data class User(
    val email: String,
    val is_active: Boolean
)

/**
 * Mirrors: profiles table
 */
data class Profile(
    val id: String,
    val user_id: String,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val langue: Langue
)

/**
 * Mirrors: organisations table
 */
data class Organisation(
    val denomination: String,
    val nif: String,
    val nis: String,
    val registre_commerce: String,
    val adresse: String,
    val wilaya: String,
    val commune: String,
    val type: OrganisationType,
    val is_verified: Boolean
)

/**
 * Mirrors: operateurs_economiques table
 */
data class OperateurEconomique(
    val qualifications: List<String>,
    val categories: List<String>,
    val is_eligible: Boolean,
    val is_blacklisted: Boolean,
    val raison_blacklist: String?
)

/**
 * Aggregated profile data for the UI
 */
data class ProfileScreenData(
    val user: User,
    val profile: Profile,
    val organisation: Organisation,
    val operateur: OperateurEconomique
) {
    val fullName: String
        get() = "${profile.prenom} ${profile.nom}"

    val initials: String
        get() = "${profile.prenom.firstOrNull()?.uppercase() ?: ""}${profile.nom.firstOrNull()?.uppercase() ?: ""}"
}


