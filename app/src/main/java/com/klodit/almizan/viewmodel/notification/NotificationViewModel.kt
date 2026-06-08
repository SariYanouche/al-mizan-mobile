package com.klodit.almizan.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.remote.ApiClient
import com.klodit.almizan.data.repository.NotificationRepository
import com.klodit.almizan.model.NotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotificationUiState {
    object Loading                                       : NotificationUiState()
    data class Success(val items: List<NotificationDto>) : NotificationUiState()
    data class Error(val message: String)                : NotificationUiState()
}

class NotificationViewModel : ViewModel() {

    private val repo = NotificationRepository(ApiClient.notificationApi)

    private val _uiState     = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val allItems      = mutableListOf<NotificationDto>()
    private var currentUserId = ""

    // ── Entry point called from MainScreen / NotificationScreen ───────────────
    fun loadForUser(userId: String) {
        if (currentUserId == userId && allItems.isNotEmpty()) return
        currentUserId = userId
        refresh()
    }

    // ── Refresh (re-fetch from API) ───────────────────────────────────────────
    fun refresh() {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            allItems.clear()

            repo.getMyNotifications(currentUserId).fold(
                onSuccess = { paginated ->
                    allItems.addAll(paginated.data)
                    _uiState.value     = NotificationUiState.Success(allItems.toList())
                    _unreadCount.value = allItems.count { !it.isLue }
                },
                onFailure = {
                    android.util.Log.e("NOTIF", "API failed (${it.message}), using mock")
                    // ── Fallback mock ─────────────────────────────────────────
                    val mock = listOf(
                        NotificationDto(
                            id = "1", userId = currentUserId,
                            titre = "Nouvel appel d'offres publié",
                            contenu = "Un appel d'offres de travaux publics a été publié à Alger.",
                            type = "PLATEFORME", categorie = "PUBLICATION",
                            statut = "ENVOYE", isLue = false,
                            dateEnvoi = "2026-07-16T10:30:00Z", dateLecture = null,
                            destinataire = null, refEntiteId = "AO-2026-001",
                            refEntiteType = "TENDER", createdAt = "2026-06-16T10:30:00Z"
                        ),
                        NotificationDto(
                            id = "2", userId = currentUserId,
                            titre = "Marché attribué",
                            contenu = "Le marché AO-2026-145 a été attribué à SARL TechBuild.",
                            type = "PLATEFORME", categorie = "ATTRIBUTION",
                            statut = "ENVOYE", isLue = true,
                            dateEnvoi = "2026-06-15T08:15:00Z", dateLecture = "2026-06-15T09:00:00Z",
                            destinataire = null, refEntiteId = "AO-2026-145",
                            refEntiteType = "TENDER", createdAt = "2026-06-15T08:15:00Z"
                        )
                    )
                    allItems.addAll(mock)
                    _uiState.value     = NotificationUiState.Success(mock)
                    _unreadCount.value = mock.count { !it.isLue }
                }
            )
        }
    }

    fun fetchUnreadCount() {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            repo.getUnreadCount(currentUserId).onSuccess { _unreadCount.value = it }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repo.markAsRead(currentUserId, id).onSuccess {
                val updated = allItems.map { n -> if (n.id == id) n.copy(isLue = true) else n }
                allItems.clear(); allItems.addAll(updated)
                _uiState.value     = NotificationUiState.Success(allItems.toList())
                _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repo.markAllAsRead(currentUserId).onSuccess {
                val updated = allItems.map { n -> n.copy(isLue = true) }
                allItems.clear(); allItems.addAll(updated)
                _uiState.value     = NotificationUiState.Success(allItems.toList())
                _unreadCount.value = 0
            }
        }
    }
}