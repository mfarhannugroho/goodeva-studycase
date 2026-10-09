package com.goodeva.bletracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val devices: List<BleDevice> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showClearDialog: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _showClearDialog = MutableStateFlow(false)

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.getAllHistory(),
        _searchQuery,
        _showClearDialog
    ) { historyList, query, showDialog ->
        val filtered = if (query.isBlank()) {
            historyList
        } else {
            val q = query.trim().lowercase()
            historyList.filter {
                (it.name?.lowercase()?.contains(q) == true) ||
                        it.macAddress.lowercase().contains(q)
            }
        }
        HistoryUiState(
            devices = filtered,
            searchQuery = query,
            showClearDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun openClearDialog() {
        _showClearDialog.value = true
    }

    fun dismissClearDialog() {
        _showClearDialog.value = false
    }

    fun confirmClearHistory() {
        _showClearDialog.value = false
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
