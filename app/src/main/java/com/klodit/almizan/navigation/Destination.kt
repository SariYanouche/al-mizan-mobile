package com.klodit.almizan.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Destination(val route: String) {

    // ── Auth ──────────────────────────────────────────────────────────────────
    object Login            : Destination("login")
    object ForgotPassword   : Destination("forgot_password")
    object SetNewPassword   : Destination("set_new_password")
    object AccountLocked    : Destination("account_locked")
    object Terms            : Destination("terms")
    object Privacy          : Destination("privacy")


    data object Verification : Destination("verification/{email}") {
        fun route(email: String) = "verification/$email"
        val arguments = listOf(navArgument("email") { type = NavType.StringType })
    }

    data object OtpVerification : Destination("otp/{email}") {
        fun route(email: String) = "otp/${Uri.encode(email)}"
        val arguments = listOf(navArgument("email") { type = NavType.StringType })
    }

    // ── Registration ──────────────────────────────────────────────────────────
    object RegistrationStep1 : Destination("registration/step1")
    object RegistrationStep2 : Destination("registration/step2")
    object RegistrationStep3 : Destination("registration/step3")

    // ── Main ──────────────────────────────────────────────────────────────────
    object Main   : Destination("main")
    object Filter : Destination("filter")

    // ── Tender ────────────────────────────────────────────────────────────────
    data object TenderDetail : Destination("tender/{tenderId}") {
        fun route(tenderId: String) = "tender/$tenderId"
        val arguments = listOf(navArgument("tenderId") { type = NavType.StringType })
    }

    // ── Profile ───────────────────────────────────────────────────────────────
    object ChangePassword : Destination("profile/change-password")

    data object EditProfile : Destination("profile/edit/{profileId}") {
        fun route(profileId: String) = "profile/edit/$profileId"
        val arguments = listOf(navArgument("profileId") { type = NavType.StringType })
    }

    data object DeleteAccount : Destination("profile/delete/{profileId}") {
        fun route(profileId: String) = "profile/delete/$profileId"
        val arguments = listOf(navArgument("profileId") { type = NavType.StringType })
    }

    // ── Statistics ───────────────────────────────────────────────────────────
    object Statistics : Destination("statistics")
}