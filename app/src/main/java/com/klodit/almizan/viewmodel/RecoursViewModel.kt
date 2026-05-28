package com.klodit.almizan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.repository.TenderRepository
import com.klodit.almizan.data.api.AttributionDto
import com.klodit.almizan.data.api.TenderDto
import com.klodit.almizan.data.api.RecoursRecord
import com.klodit.almizan.data.repository.RecoursRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecoursViewModel : ViewModel() {
    private val repository = RecoursRepository()

    private val _recoursList = MutableStateFlow<List<RecoursRecord>>(emptyList())
    val recoursList: StateFlow<List<RecoursRecord>> = _recoursList.asStateFlow()

    private val _recoursDetail = MutableStateFlow<RecoursRecord?>(null)
    val recoursDetail: StateFlow<RecoursRecord?> = _recoursDetail.asStateFlow()

    // ── Real API Data for Appeals Dropdown ──
    private val _availableAos = MutableStateFlow<List<TenderDto>>(emptyList())
    val availableAos: StateFlow<List<TenderDto>> = _availableAos.asStateFlow()

    private val _availableAttributions = MutableStateFlow<List<AttributionDto>>(emptyList())
    val availableAttributions: StateFlow<List<AttributionDto>> = _availableAttributions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submitResult = MutableStateFlow<Result<String>?>(null)
    val submitResult: StateFlow<Result<String>?> = _submitResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Loads all appeals for the currently authenticated operateur.
     * The operateurId is resolved dynamically by RecoursRepository → ProfileRepository.getCurrentOperateurId()
     * via the /auth/me → /users/operateurs-economiques chain.
     */
    fun loadMesRecours() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.getMesRecours()
                if (response.isSuccessful) {
                    _recoursList.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Error ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Connection error"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAppealOptions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val aosResult = TenderRepository.getAvailableTenders()
                val attrResult = TenderRepository.getAttributions()

                if (aosResult.isSuccess) {
                    _availableAos.value = aosResult.getOrNull() ?: emptyList()
                }
                if (attrResult.isSuccess) {
                    _availableAttributions.value = attrResult.getOrNull()?.filter {
                        it.type?.uppercase() == "PROVISOIRE"
                    } ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Loading error"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Submits an appeal. The operateurId is dynamically fetched inside
     * RecoursRepository.submitAppeal() via ProfileRepository.getCurrentOperateurId().
     */
    fun submitAppeal(aoId: String, attributionId: String, motif: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.submitAppeal(aoId, attributionId, motif)
            _submitResult.value = result
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }
    
    fun resetSubmitResult() {
        _submitResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}