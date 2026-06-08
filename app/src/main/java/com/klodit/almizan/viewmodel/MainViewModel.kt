package com.klodit.almizan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.klodit.almizan.ui.components.BottomNavDestination
import com.klodit.almizan.ui.theme.AppLanguage
import com.klodit.almizan.util.LocaleHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentRoute = MutableStateFlow(BottomNavDestination.Home.route)
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    // ── User identity ─────────────────────────────────────────────────────────
    // FIX: userId and profileId were missing — the profile screen needs both
    private val _userId    = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _profileId = MutableStateFlow("")
    val profileId: StateFlow<String> = _profileId.asStateFlow()

    private val _userFirstName = MutableStateFlow("")
    val userFirstName: StateFlow<String> = _userFirstName.asStateFlow()

    private val _userLastName = MutableStateFlow("")
    val userLastName: StateFlow<String> = _userLastName.asStateFlow()

    private val _isVerified = MutableStateFlow(false)
    val isVerified: StateFlow<Boolean> = _isVerified.asStateFlow()

    private val _tier = MutableStateFlow("OUVERT")
    val tier: StateFlow<String> = _tier.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // ── Language ──────────────────────────────────────────────────────────────
    private val _language = MutableStateFlow(LocaleHelper.currentLanguage(application))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // ── Navigation ────────────────────────────────────────────────────────────
    // Bid Wizard navigation state
    // TODO: Replace with proper navigation when integrating with NavController
    private val _showBidWizard = MutableStateFlow(false)
    val showBidWizard: StateFlow<Boolean> = _showBidWizard.asStateFlow()

    private val _currentBidAppelOffreId = MutableStateFlow<String?>(null)
    val currentBidAppelOffreId: StateFlow<String?> = _currentBidAppelOffreId.asStateFlow()

    // Bid Status screen state
    private val _showBidStatus = MutableStateFlow(false)
    val showBidStatus: StateFlow<Boolean> = _showBidStatus.asStateFlow()

    private val _currentStatusSubmissionId = MutableStateFlow<String?>(null)
    val currentStatusSubmissionId: StateFlow<String?> = _currentStatusSubmissionId.asStateFlow()

    // Evaluation Results screen state
    private val _showEvaluationResults = MutableStateFlow(false)
    val showEvaluationResults: StateFlow<Boolean> = _showEvaluationResults.asStateFlow()

    private val _currentEvalSubmissionId = MutableStateFlow<String?>(null)
    val currentEvalSubmissionId: StateFlow<String?> = _currentEvalSubmissionId.asStateFlow()

    // File Appeal screen state
    private val _showFileAppeal = MutableStateFlow(false)
    val showFileAppeal: StateFlow<Boolean> = _showFileAppeal.asStateFlow()

    private val _currentAppealSubmissionId = MutableStateFlow<String?>(null)
    val currentAppealSubmissionId: StateFlow<String?> = _currentAppealSubmissionId.asStateFlow()

    // Documents screen state
    private val _showDocuments = MutableStateFlow(false)
    val showDocuments: StateFlow<Boolean> = _showDocuments.asStateFlow()

    // Security screen state
    private val _showSecurity = MutableStateFlow(false)
    val showSecurity: StateFlow<Boolean> = _showSecurity.asStateFlow()

    // Settings screen state
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()
    fun onTabSelected(destination: BottomNavDestination) {
        _currentRoute.value = destination.route
    }

    fun onLanguageChange(lang: AppLanguage) {
        LocaleHelper.setLocale(getApplication(), lang)
        _language.value = lang
    }


    //stats
    private val _showStatistics = MutableStateFlow(false)
    val showStatistics: StateFlow<Boolean> = _showStatistics

    fun openStatistics()  { _showStatistics.value = true }
    fun closeStatistics() { _showStatistics.value = false }

    // ── Auth lifecycle ────────────────────────────────────────────────────────
    fun onLogout() {
        _userId.value        = ""
        _profileId.value     = ""
        _userFirstName.value = ""
        _userLastName.value  = ""
        _isVerified.value    = false
        _tier.value          = "OUVERT"
        _unreadCount.value   = 0
    }

    fun onLogin(
        firstName : String,
        lastName  : String,
        verified  : Boolean = false,
        tier      : String  = "OUVERT"
    ) {
        _userFirstName.value = firstName
        _userLastName.value  = lastName
        _isVerified.value    = verified
        _tier.value          = tier
    }


    fun onProfileLoaded(
        userId     : String,
        profileId  : String,
        firstName  : String,
        lastName   : String,
        isVerified : Boolean,
        tier       : String
    ) {
        _userId.value        = userId
        _profileId.value     = profileId
        _userFirstName.value = firstName
        _userLastName.value  = lastName
        _isVerified.value    = isVerified
        _tier.value          = tier
    }

    fun setUnreadCount(count: Int) {
        _unreadCount.value = count
    }

    fun openBidWizard(appelOffreId: String) {
        _currentBidAppelOffreId.value = appelOffreId
        _showBidWizard.value = true
    }

    fun closeBidWizard() {
        _showBidWizard.value = false
        _currentBidAppelOffreId.value = null
    }

    fun openBidStatus(submissionId: String) {
        _currentStatusSubmissionId.value = submissionId
        _showBidStatus.value = true
    }

    fun closeBidStatus() {
        _showBidStatus.value = false
        _currentStatusSubmissionId.value = null
    }

    fun openEvaluationResults(submissionId: String) {
        _currentEvalSubmissionId.value = submissionId
        _showEvaluationResults.value = true
    }

    fun closeEvaluationResults() {
        _showEvaluationResults.value = false
        _currentEvalSubmissionId.value = null
    }

    fun openFileAppeal(submissionId: String) {
        _currentAppealSubmissionId.value = submissionId
        _showFileAppeal.value = true
    }

    fun closeFileAppeal() {
        _showFileAppeal.value = false
        _currentAppealSubmissionId.value = null
    }

    fun openDocuments() {
        _showDocuments.value = true
    }

    fun closeDocuments() {
        _showDocuments.value = false
    }

    fun openSecurity() {
        _showSecurity.value = true
    }

    fun closeSecurity() {
        _showSecurity.value = false
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    fun openNotifications()  { _showNotifications.value = true  }
    fun closeNotifications() { _showNotifications.value = false }



}
