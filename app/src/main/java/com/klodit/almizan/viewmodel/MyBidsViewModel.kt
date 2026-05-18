package com.klodit.almizan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.repository.TenderRepository
import com.klodit.almizan.data.repository.SoumissionRepository
import com.klodit.almizan.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyBidsViewModel : ViewModel() {
    private val soumissionRepository = SoumissionRepository()

    private val _cdcs = MutableStateFlow<List<PurchasedCDC>>(emptyList())
    val cdcs: StateFlow<List<PurchasedCDC>> = _cdcs.asStateFlow()

    private val _submissions = MutableStateFlow<List<BidSubmission>>(emptyList())
    val submissions: StateFlow<List<BidSubmission>> = _submissions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun mapApiStatus(status: String?): SoumissionStatut? {
        val raw = status?.uppercase() ?: return null
        return when {
            raw.contains("BROUILLON") -> SoumissionStatut.BROUILLON
            raw.contains("DEPOSE") || raw.contains("SOUMIS") -> SoumissionStatut.DEPOSEE
            raw.contains("RECU") -> SoumissionStatut.RECUE
            raw.contains("EVAL") -> SoumissionStatut.EVALUEE
            raw.contains("RETEN") || raw.contains("ATTRIB") -> SoumissionStatut.RETENUE
            raw.contains("REJET") -> SoumissionStatut.REJETEE
            else -> null
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val aosResult = TenderRepository.getAvailableTenders()
            val subsResult = soumissionRepository.getMySubmissions()

            if (aosResult.isSuccess && subsResult.isSuccess) {
                val aos = aosResult.getOrNull() ?: emptyList()
                val subs = subsResult.getOrNull() ?: emptyList()

                _cdcs.value = aos.map { ao ->
                    val sub = subs.find { it.appelOffreId == ao.id || it.appel_offre_id == ao.id }
                    PurchasedCDC(
                        appelOffreId = ao.id,
                        soumissionId = sub?.id,
                        reference = ao.reference ?: "",
                        objet = ao.objet ?: "",
                        dateLimiteDepot = ao.dateLimiteSoumission ?: "",
                        statutSoumission = mapApiStatus(sub?.statut ?: sub?.status)
                    )
                }

                _submissions.value = subs.mapNotNull { sub ->
                    val aoId = sub.appelOffreId ?: sub.appel_offre_id ?: return@mapNotNull null
                    val ao = aos.find { it.id == aoId }
                    val status = mapApiStatus(sub.statut ?: sub.status) ?: SoumissionStatut.DEPOSEE
                    
                    if (status == SoumissionStatut.BROUILLON) null // Only submitted offers in My Bids tab
                    else {
                        BidSubmission(
                            soumissionId = sub.id,
                            appelOffreId = aoId,
                            reference = sub.reference ?: ao?.reference ?: "",
                            objet = ao?.objet ?: "",
                            dateDepot = sub.horodatageServeur ?: sub.horodatage_serveur ?: sub.createdAt ?: sub.created_at ?: "",
                            statut = status
                        )
                    }
                }
            }
            _isLoading.value = false
        }
    }
}