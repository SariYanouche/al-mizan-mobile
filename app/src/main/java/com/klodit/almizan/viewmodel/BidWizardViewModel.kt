package com.klodit.almizan.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.R
import com.klodit.almizan.data.repository.TenderRepository
import com.klodit.almizan.data.repository.SoumissionRepository
import com.klodit.almizan.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BidWizardViewModel : ViewModel() {

    private val soumissionRepository = SoumissionRepository()
    private val tenderRepository = TenderRepository

    private val _uiState = MutableStateFlow(BidWizardState())
    val uiState: StateFlow<BidWizardState> = _uiState.asStateFlow()

    // UI Loading & Error states
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    private val _submissionResult = MutableStateFlow<SubmissionResult?>(null)
    val submissionResult: StateFlow<SubmissionResult?> = _submissionResult.asStateFlow()

    fun initWizard(aoId: String?) {
        _submitError.value = null

        viewModelScope.launch {
            val result = TenderRepository.getAvailableTenders()
            
            result.onSuccess { dtos ->
                val aos = dtos.map { dto ->
                    AoOption(
                        id = dto.id,
                        reference = dto.reference ?: dto.id,
                        objet = dto.objet ?: "",
                        type = mapAoType(dto.typeProcedure),
                        status = dto.statut ?: "INCONNU",
                        organisationName = dto.organisationName ?: "",
                        wilaya = dto.wilaya ?: "",
                        deadline = dto.dateLimiteSoumission ?: "",
                        lots = dto.lots?.map { lot -> 
                            Lot(
                                id = lot.id,
                                appelOffreId = dto.id,
                                numero = lot.numero?.toIntOrNull() ?: 1,
                                designation = lot.designation ?: "",
                                description = lot.description,
                                montantEstime = lot.montantEstime?.toString()
                            )
                        } ?: emptyList()
                    )
                }
                
                _uiState.update { it.copy(availableAos = aos) }

                // If the user clicked "Start Bid" from a specific AO, pre-select it
                if (!aoId.isNullOrEmpty()) {
                    selectAo(aoId)
                }
            }
        }
    }

    private fun mapAoType(rawType: String?): String {
        val raw = rawType?.uppercase() ?: ""
        if (raw.contains("RESTREINT")) return "AO restreint"
        if (raw.contains("GRE")) return "Gré à gré"
        return "AO ouvert"
    }

    fun selectAo(aoId: String) {
        val ao = _uiState.value.availableAos.find { it.id == aoId }
        _uiState.update { state ->
            state.copy(
                selectedAoId = aoId,
                appelOffreId = aoId,
                appelOffreReference = ao?.reference ?: "",
                appelOffreObjet = ao?.objet ?: "",
                availableLots = ao?.lots ?: emptyList(),
                selectedLotId = null, // Reset lot selection when AO changes
                lotBpus = emptyList() // Reset BPUs
            )
        }
    }


    fun selectLot(lotId: String) {
        _uiState.update { it.copy(selectedLotId = lotId) }
        val currentBpu = _uiState.value.lotBpus.find { it.lotId == lotId }
        if (currentBpu == null) {
            val initialLine = BpuLine()
            _uiState.update { it.copy(lotBpus = listOf(LotBpu(lotId, listOf(initialLine)))) }
        }
    }


    fun updateTechOffer(document: UploadedDocument?) {
        _uiState.update { it.copy(offreTechnique = OffreTechnique(document)) }
    }

    fun updateBpuLine(lotId: String, lineId: String, field: String, value: String) {
        _uiState.update { state ->
            val updatedBpus = state.lotBpus.map { bpu ->
                if (bpu.lotId == lotId) {
                    val updatedLines = bpu.lines.map { line ->
                        if (line.id == lineId) {
                            when (field) {
                                "designation" -> line.copy(designation = value)
                                "unite" -> line.copy(unite = value)
                                "quantite" -> line.copy(quantite = value)
                                "prixUnitaire" -> line.copy(prixUnitaire = value)
                                else -> line
                            }
                        } else line
                    }
                    bpu.copy(lines = updatedLines)
                } else bpu
            }
            state.copy(lotBpus = updatedBpus)
        }
    }

    fun addBpuLine(lotId: String) {
        _uiState.update { state ->
            val updatedBpus = state.lotBpus.map { bpu ->
                if (bpu.lotId == lotId) bpu.copy(lines = bpu.lines + BpuLine()) else bpu
            }
            state.copy(lotBpus = updatedBpus)
        }
    }

    fun removeBpuLine(lotId: String, lineId: String) {
        _uiState.update { state ->
            val updatedBpus = state.lotBpus.map { bpu ->
                if (bpu.lotId == lotId && bpu.lines.size > 1) {
                    bpu.copy(lines = bpu.lines.filter { it.id != lineId })
                } else bpu
            }
            state.copy(lotBpus = updatedBpus)
        }
    }

    fun updateCaution(caution: CautionData) {
        _uiState.update { it.copy(caution = caution) }
    }

    fun toggleCertification(accepted: Boolean) {
        _uiState.update { it.copy(certificationAccepted = accepted) }
    }

    fun nextStep() {
        if (_uiState.value.currentStep < _uiState.value.totalSteps) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun prevStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun submitBid(context: Context) {
        _isSubmitting.value = true
        _submitError.value = null

        viewModelScope.launch {
            val result = soumissionRepository.submitBidWorkflow(_uiState.value, context)
            
            _isSubmitting.value = false
            
            result.onSuccess { res ->
                _submissionResult.value = res
            }.onFailure { err ->
                _submitError.value = err.message ?: context.getString(R.string.wizard_error_unknown)
            }
        }
    }
}