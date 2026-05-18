package com.klodit.almizan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klodit.almizan.data.api.SoumissionDetailDto
import com.klodit.almizan.data.repository.SoumissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SoumissionDetailViewModel : ViewModel() {
    private val repository = SoumissionRepository()

    private val _detail = MutableStateFlow<SoumissionDetailDto?>(null)
    val detail: StateFlow<SoumissionDetailDto?> = _detail.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.getSubmissionById(id)
            if (res.isSuccess) {
                _detail.value = res.getOrNull()
            }
            _isLoading.value = false
        }
    }
}