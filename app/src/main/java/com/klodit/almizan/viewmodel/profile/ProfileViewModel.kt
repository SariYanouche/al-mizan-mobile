package com.klodit.almizan.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.profile.DeleteUiState
import com.klodit.almizan.data.profile.ProfileData
import com.klodit.almizan.data.profile.ProfileUiState
import com.klodit.almizan.data.profile.UpdateProfileRequest
import com.klodit.almizan.data.profile.UpdateUiState
import com.klodit.almizan.data.repository.ProfileRepository
import com.klodit.almizan.ui.profile.DocumentUiModel
import com.klodit.almizan.ui.profile.ProfileScreenData
import com.klodit.almizan.ui.profile.security.Session
import com.klodit.almizan.ui.profile.security.UserSecurity
import com.klodit.almizan.ui.profile.settings.AuditLog
import com.klodit.almizan.ui.profile.settings.NotificationCategory
import com.klodit.almizan.ui.profile.settings.NotificationChannel
import com.klodit.almizan.ui.profile.settings.NotificationPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()

    // ── Profile ──
    private val _profileData = MutableStateFlow<ProfileScreenData?>(null)
    val profileData: StateFlow<ProfileScreenData?> = _profileData.asStateFlow()

    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    private val _updateUiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateUiState: StateFlow<UpdateUiState> = _updateUiState.asStateFlow()

    private val _deleteUiState = MutableStateFlow<DeleteUiState>(DeleteUiState.Idle)
    val deleteUiState: StateFlow<DeleteUiState> = _deleteUiState.asStateFlow()

    // ── Security ──
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    private val _userSecurity = MutableStateFlow<UserSecurity?>(null)
    val userSecurity: StateFlow<UserSecurity?> = _userSecurity.asStateFlow()

    private val _passwordLastChangedDays = MutableStateFlow(0)
    val passwordLastChangedDays: StateFlow<Int> = _passwordLastChangedDays.asStateFlow()

    // ── Documents ──
    private val _documents = MutableStateFlow<List<DocumentUiModel>>(emptyList())
    val documents: StateFlow<List<DocumentUiModel>> = _documents.asStateFlow()

    // ── Settings ──
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _notificationPreference = MutableStateFlow(
        NotificationPreference(
            channels = NotificationChannel.entries.associateWith { true },
            categories = NotificationCategory.entries.associateWith { true }
        )
    )
    val notificationPreference: StateFlow<NotificationPreference> = _notificationPreference.asStateFlow()

    // ── Loading ──
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProfileScreenData().onSuccess { _profileData.value = it }
            _isLoading.value = false
        }
    }

    fun fetchProfileByUserId(userId: String, token: String) {
        viewModelScope.launch {
           /* if (_profileUiState.value !is ProfileUiState.Success) {
                _profileUiState.value = ProfileUiState.Loading
            }*/
            _profileUiState.value = ProfileUiState.Loading

            repository.getProfileScreenData()
                .onSuccess { data ->
                    _profileData.value = data
                    _profileUiState.value = ProfileUiState.Success(data.toLegacyProfileData())
                }
                .onFailure { e ->
                    _profileUiState.value = ProfileUiState.Error(
                        e.localizedMessage ?: "Erreur reseau"
                    )
                }
        }
    }

    fun updateProfile(profileId: String, token: String, request: UpdateProfileRequest) {
        viewModelScope.launch {
            _updateUiState.value = UpdateUiState.Loading
            repository.updateProfile(profileId, request)
                .onSuccess {
                    _updateUiState.value = UpdateUiState.Success("Profil mis a jour avec succes")
                }
                .onFailure { e ->
                    _updateUiState.value = UpdateUiState.Error(
                        e.localizedMessage ?: "Erreur reseau"
                    )
                }
        }
    }

    // deleteProfile
    fun deleteProfile(profileId: String, token: String) {
        viewModelScope.launch {
            _deleteUiState.value = DeleteUiState.Loading
            repository.deleteProfile(profileId, token)  // ← passe token ici
                .onSuccess { _deleteUiState.value = DeleteUiState.Success("Compte supprimé") }
                .onFailure { e -> _deleteUiState.value = DeleteUiState.Error(e.message ?: "Erreur") }
        }
    }



    fun resetUpdateState() { _updateUiState.value = UpdateUiState.Idle }
    fun resetDeleteState() { _deleteUiState.value = DeleteUiState.Idle }

    fun loadSecurityData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSessions().onSuccess { _sessions.value = it }
            repository.getUserSecurity().onSuccess { security ->
                _userSecurity.value = security
                // Derive passwordLastChangedDays from lastLogin as a proxy
                // until a dedicated password-change-date API is available
                _passwordLastChangedDays.value = ChronoUnit.DAYS.between(
                    security.lastLogin,
                    LocalDateTime.now()
                ).toInt().coerceAtLeast(0)
            }
            _isLoading.value = false
        }
    }

    fun loadDocumentsData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getDocuments().onSuccess { _documents.value = it }
            _isLoading.value = false
        }
    }

    fun loadSettingsData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAuditLogs().onSuccess { _auditLogs.value = it }
            _isLoading.value = false
        }
    }

    private fun ProfileScreenData.toLegacyProfileData(): ProfileData {
        val tierLabel = if (organisation.is_verified) "VERIFIE" else "OUVERT"
        return ProfileData(
            id = profile.id,
            userId = profile.user_id,
            firstName = profile.prenom,
            lastName = profile.nom,
            email = user.email,
            phone = profile.telephone,
            organizationName = organisation.denomination,
            nif = organisation.nif,
            nis = organisation.nis,
            rc = organisation.registre_commerce,
            isVerified = organisation.is_verified,
            tier = tierLabel,
            avatarUrl = null
        )
    }
}