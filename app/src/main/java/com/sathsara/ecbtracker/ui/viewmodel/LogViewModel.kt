package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import javax.inject.Inject

data class LogUiState(
    val isLoading: Boolean = false,
    val previousUnit: Double = 0.0,
    val currentUnitInput: String = "0000000",
    val selectedAppliances: Set<String> = emptySet(),
    val note: String = "",
    val photoFile: File? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        loadPreviousReading()
    }

    private fun loadPreviousReading() {
        viewModelScope.launch {
            val currentMoment = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val dateStr = currentMoment.date.toString()
            
            val prevEntry = entryRepository.getLatestEntryBefore(dateStr).getOrNull()
            if (prevEntry != null) {
                _uiState.value = _uiState.value.copy(previousUnit = prevEntry.unit)
            }
        }
    }

    fun updateUnitInput(input: String) {
        if (input.length <= 7 && input.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(currentUnitInput = input)
        }
    }

    fun toggleAppliance(appliance: String) {
        val current = _uiState.value.selectedAppliances.toMutableSet()
        if (current.contains(appliance)) {
            current.remove(appliance)
        } else {
            current.add(appliance)
        }
        _uiState.value = _uiState.value.copy(selectedAppliances = current)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun setPhoto(file: File) {
        _uiState.value = _uiState.value.copy(photoFile = file)
    }

    fun submitReading() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val state = _uiState.value
            
            // Parse 5 digits + 2 decimals from string like "04827" -> 48.27
            val parsedDouble = if (state.currentUnitInput.length == 7) {
                val integerPart = state.currentUnitInput.substring(0, 5)
                val decimalPart = state.currentUnitInput.substring(5, 7)
                "$integerPart.$decimalPart".toDoubleOrNull()
            } else {
                null
            }

            if (parsedDouble == null || parsedDouble < state.previousUnit) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Invalid reading. Must be 7 digits and greater than previous."
                )
                return@launch
            }

            val usedAmount = parsedDouble - state.previousUnit
            val currentMoment = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val timeStr = "${currentMoment.hour.toString().padStart(2, '0')}:${currentMoment.minute.toString().padStart(2, '0')}"

            val entry = Entry(
                date = currentMoment.date.toString(),
                time = timeStr,
                unit = parsedDouble,
                used = usedAmount,
                note = state.note.takeIf { it.isNotBlank() },
                appliances = state.selectedAppliances.toList()
            )

            entryRepository.insertEntry(entry, state.photoFile)
                .onSuccess {
                    _uiState.value = state.copy(isLoading = false, isSuccess = true)
                }
                .onFailure {
                    _uiState.value = state.copy(isLoading = false, error = it.message ?: "Failed to save reading")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
