package com.klodit.almizan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.repository.TenderRepository
import com.klodit.almizan.model.tender.Tender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeStats(
    val activeTenders: Int = 0,
    val awarded: Int = 0,
    val total: Int = 0
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val stats: HomeStats,
        val latestTenders: List<Tender>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TenderRepository

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            repository.getAvailableTenders()
                .onSuccess { tenders ->
                    val active = tenders.filter {
                        it.statut?.uppercase() in listOf("PUBLIE", "EN_COURS", "OUVERTURE_PLIS")
                    }
                    val awarded = tenders.filter {
                        it.statut?.uppercase() == "ATTRIBUE"
                    }
                    // Latest 3 active tenders sorted by createdAt desc
                    val latest = active
                        .sortedByDescending { it.createdAt }
                        .take(3)
                        .map { dto ->
                            Tender(
                                id                   = dto.id,
                                reference            = dto.reference ?: "",
                                objet                = dto.objet ?: "",
                                typeProcedure        = dto.typeProcedure ?: "",
                                montantEstime        = dto.montantEstime?.toString(),
                                datePublication      = dto.datePublication,
                                dateLimiteSoumission = dto.dateLimiteSoumission,
                                dateLimiteRetraitCdc = dto.dateLimiteRetraitCdc,
                                statut               = dto.statut ?: "",
                                serviceContractantId = dto.serviceContractantId ?: "",
                                wilaya               = dto.wilaya ?: "",
                                secteurActivite      = dto.secteurActivite ?: "",
                                createdAt            = dto.createdAt ?: "",
                                lots                 = emptyList()
                            )
                        }

                    _uiState.value = HomeUiState.Success(
                        stats = HomeStats(
                            activeTenders = active.size,
                            awarded       = awarded.size,
                            total         = tenders.size
                        ),
                        latestTenders = latest
                    )
                }
                .onFailure { e ->
                    _uiState.value = HomeUiState.Error(e.message ?: "Erreur inconnue")
                }
        }
    }
}