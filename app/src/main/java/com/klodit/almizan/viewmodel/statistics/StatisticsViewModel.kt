package com.klodit.almizan.viewmodel.statistics


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.repository.TenderRepository
import com.klodit.almizan.data.api.TenderDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

// ─── Models ───────────────────────────────────────────────────────────────────

data class StatisticsData(
    // Overview
    val totalActive       : Int,
    val totalAttributed   : Int,
    val expiringIn7Days   : Int,
    val totalCancelled    : Int,
    val totalPublished    : Int,

    // By status
    val byStatus          : Map<String, Int>,

    // By sector
    val bySector          : List<Pair<String, Int>>,   // sorted desc

    // By procedure type
    val byProcedureType   : List<Pair<String, Int>>,   // sorted desc

    // By wilaya
    val byWilaya          : List<Pair<String, Int>>,   // sorted desc

    // Monthly publications (last 6 months)
    val monthlyPublications : List<Pair<String, Int>>, // label → count

    // Highlights
    val topWilaya         : String,
    val topWilayaCount    : Int,
    val averageMontant    : Double,   // in DA
    val montantDelta      : Double,   // % change vs previous period (placeholder)
)

sealed class StatisticsUiState {
    object Loading                          : StatisticsUiState()
    data class Success(val data: StatisticsData) : StatisticsUiState()
    data class Error(val message: String)   : StatisticsUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TenderRepository

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            try {
                val tenderResult     = repository.getAvailableTenders()
                val attributionResult = repository.getAttributions()

                if (tenderResult.isFailure) {
                    _uiState.value = StatisticsUiState.Error(
                        tenderResult.exceptionOrNull()?.message ?: "Erreur inconnue"
                    )
                    return@launch
                }

                val tenders     = tenderResult.getOrDefault(emptyList())
                val attributions = attributionResult.getOrDefault(emptyList())
                val now         = OffsetDateTime.now()

                // ── Overview ──────────────────────────────────────────────────
                val activeStatuses = setOf("PUBLIE", "EN_COURS", "OUVERTURE_PLIS", "EVALUATION")
                val totalActive    = tenders.count { it.statut?.uppercase() in activeStatuses }
                val totalAttributed = attributions.size
                val totalCancelled = tenders.count {
                    it.statut?.uppercase() in setOf("ANNULE", "CLOTURE")
                }
                val totalPublished = tenders.size

                val expiringIn7Days = tenders.count { tender ->
                    try {
                        val deadline = OffsetDateTime.parse(tender.dateLimiteSoumission)
                        val days = ChronoUnit.DAYS.between(now, deadline)
                        days in 0..7
                    } catch (e: Exception) { false }
                }

                // ── By status ─────────────────────────────────────────────────
                val statusOrder = listOf(
                    "PUBLIE", "EN_COURS", "OUVERTURE_PLIS",
                    "EVALUATION", "ATTRIBUE", "ANNULE", "CLOTURE"
                )
                val rawByStatus = tenders
                    .groupBy { it.statut?.uppercase() ?: "INCONNU" }
                    .mapValues { it.value.size }

                val byStatus = statusOrder
                    .filter { rawByStatus.containsKey(it) }
                    .associateWith { rawByStatus[it] ?: 0 }
                    .plus(rawByStatus.filterKeys { it !in statusOrder })

                // ── By sector ─────────────────────────────────────────────────
                val bySector = tenders
                    .filter { !it.secteurActivite.isNullOrBlank() }
                    .groupBy { it.secteurActivite!! }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .map { it.key to it.value }

                // ── By procedure type ─────────────────────────────────────────
                val byProcedureType = tenders
                    .filter { !it.typeProcedure.isNullOrBlank() }
                    .groupBy { it.typeProcedure!! }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .map { it.key to it.value }

                // ── By wilaya ─────────────────────────────────────────────────
                val byWilaya = tenders
                    .filter { !it.wilaya.isNullOrBlank() }
                    .groupBy { it.wilaya!! }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .map { it.key to it.value }

                // ── Monthly publications (last 6 months) ──────────────────────
                val monthLabels = (5 downTo 0).map { offset ->
                    now.minusMonths(offset.toLong())
                }
                val monthlyPublications = monthLabels.map { month ->
                    val label = month.month.name.take(3)
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                    val count = tenders.count { tender ->
                        try {
                            val pub = OffsetDateTime.parse(tender.datePublication)
                            pub.year == month.year && pub.month == month.month
                        } catch (e: Exception) { false }
                    }
                    label to count
                }

                // ── Highlights ────────────────────────────────────────────────
                val topWilayaEntry = byWilaya.firstOrNull()
                val topWilaya      = topWilayaEntry?.first ?: "—"
                val topWilayaCount = topWilayaEntry?.second ?: 0

                val montantsWithValues = tenders.mapNotNull { it.montantEstime }
                val averageMontant     = if (montantsWithValues.isNotEmpty())
                    montantsWithValues.average() else 0.0

                _uiState.value = StatisticsUiState.Success(
                    StatisticsData(
                        totalActive          = totalActive,
                        totalAttributed      = totalAttributed,
                        expiringIn7Days      = expiringIn7Days,
                        totalCancelled       = totalCancelled,
                        totalPublished       = totalPublished,
                        byStatus             = byStatus,
                        bySector             = bySector,
                        byProcedureType      = byProcedureType,
                        byWilaya             = byWilaya,
                        monthlyPublications  = monthlyPublications,
                        topWilaya            = topWilaya,
                        topWilayaCount       = topWilayaCount,
                        averageMontant       = averageMontant,
                        montantDelta         = 8.0  // placeholder — replace with real delta when API supports it
                    )
                )

            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }
}