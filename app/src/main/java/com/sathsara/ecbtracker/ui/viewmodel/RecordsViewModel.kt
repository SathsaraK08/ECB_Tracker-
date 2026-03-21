package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.repository.EntryRepository
import com.sathsara.ecbtracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordsUiState(
    val isLoading: Boolean = true,
    val entries: List<Entry> = emptyList(),
    val ratePerUnit: Double = 32.0,
    val filterMode: FilterMode = FilterMode.ALL,
    val error: String? = null
)

enum class FilterMode { ALL, VERIFIED, PENDING }

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val rateResult = settingsRepository.getSettings()
            val rate = rateResult.getOrNull()?.lkrPerUnit ?: 32.0

            val filterVal = when (_uiState.value.filterMode) {
                FilterMode.ALL -> null
                FilterMode.VERIFIED -> true
                FilterMode.PENDING -> false
            }

            entryRepository.getEntries(limit = 100, isVerified = filterVal)
                .onSuccess { entries ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        entries = entries,
                        ratePerUnit = rate
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Failed to load records"
                    )
                }
        }
    }

    fun setFilterMode(mode: FilterMode) {
        _uiState.value = _uiState.value.copy(filterMode = mode)
        loadData()
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            entryRepository.deleteEntry(id)
                .onSuccess { loadData() }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message ?: "Failed to delete")
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
