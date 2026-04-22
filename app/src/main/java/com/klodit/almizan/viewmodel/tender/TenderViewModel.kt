package com.klodit.almizan.viewmodel.tender

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.repository.TenderRepository
import com.klodit.almizan.data.api.TenderDto
import com.klodit.almizan.model.tender.Tender
import com.klodit.almizan.model.tender.TenderLot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─── Detail state ─────────────────────────────────────────────────────────────
sealed class TenderDetailState {
    object Loading : TenderDetailState()
    data class Success(val tender: Tender) : TenderDetailState()
    data class Error(val message: String) : TenderDetailState()
}

private fun TenderDto.toUiModel(): Tender {
    val mappedLots = (lots ?: emptyList()).map { lot ->
        TenderLot(
            id            = lot.id,
            aoId          = id,
            numero        = lot.numero ?: "",
            designation   = lot.designation ?: "",
            montantEstime = lot.montantEstime?.toString(),
            statut        = lot.statut
        )
    }

    return Tender(
        id                   = id,
        reference            = reference ?: "",
        objet                = objet ?: "",
        typeProcedure        = typeProcedure ?: "",
        montantEstime        = montantEstime?.toString(),
        datePublication      = datePublication,
        dateLimiteSoumission = dateLimiteSoumission,
        dateLimiteRetraitCdc = dateLimiteRetraitCdc,
        statut               = statut ?: "",
        serviceContractantId = serviceContractantId ?: "",
        wilaya               = wilaya ?: "",
        secteurActivite      = secteurActivite ?: "",
        createdAt            = createdAt ?: "",
        lots                 = mappedLots
    )
}

// ─── List ViewModel ───────────────────────────────────────────────────────────
class TenderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TenderRepository

    private val _tenders = MutableStateFlow<List<Tender>>(emptyList())
    val tenders: StateFlow<List<Tender>> = _tenders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchTenders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value     = null
            repository.getAvailableTenders()
                .onSuccess { tenders ->
                    _tenders.value = tenders.map { it.toUiModel() }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }
}

// ─── Detail ViewModel ─────────────────────────────────────────────────────────
class TenderDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TenderRepository

    private val _state = MutableStateFlow<TenderDetailState>(TenderDetailState.Loading)
    val state: StateFlow<TenderDetailState> = _state

    fun fetchTender(tenderId: String) {
        viewModelScope.launch {
            _state.value = TenderDetailState.Loading
            repository.getTenderById(tenderId)
                .onSuccess { _state.value = TenderDetailState.Success(it.toUiModel()) }
                .onFailure { _state.value = TenderDetailState.Error(it.message ?: "Unknown error") }
        }
    }
}