package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.repository.EntryRepository
import com.sathsara.ecbtracker.data.repository.PaymentRepository
import com.sathsara.ecbtracker.data.service.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class ReportsUiState(
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,
    val exportError: String? = null,
    val fromDate: String = "",
    val toDate: String = ""
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val exportManager: ExportManager,
    private val entryRepository: EntryRepository,
    private val paymentRepository: PaymentRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    // Preferences from DataStore
    val isMonthlyEmailEnabled = dataStoreManager.monthlyEmailReport.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val isWeeklySummaryEnabled = dataStoreManager.weeklySummary.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    init {
        // Set default dates to current month
        val currentMoment = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val monthStr = currentMoment.monthNumber.toString().padStart(2, '0')
        val firstDay = "${currentMoment.year}-$monthStr-01"
        val lastDay = "${currentMoment.year}-$monthStr-31" // Simplified
        
        _uiState.value = _uiState.value.copy(
            fromDate = firstDay,
            toDate = lastDay
        )
    }

    fun setDateRange(from: String, to: String) {
        _uiState.value = _uiState.value.copy(fromDate = from, toDate = to)
    }

    fun toggleMonthlyEmail(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setMonthlyEmailReport(enabled) }
    }

    fun toggleWeeklySummary(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setWeeklySummary(enabled) }
    }

    fun exportData(format: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null, exportSuccessMessage = null)
            
            val state = _uiState.value
            val entriesResult = entryRepository.getEntriesForRange(state.fromDate, state.toDate)
            val entries = entriesResult.getOrNull() ?: emptyList()
            
            val startMonth = state.fromDate.take(7) // "YYYY-MM"
            val endMonth = state.toDate.take(7)
            val paymentsResult = paymentRepository.getPaymentsForRange(startMonth, endMonth)
            val payments = paymentsResult.getOrNull() ?: emptyList()

            val fileName = "ECB_Report_${state.fromDate}_to_${state.toDate}"

            val result = when (format.lowercase()) {
                "excel" -> exportManager.exportExcel(entries, payments, fileName)
                "csv" -> exportManager.exportCsv(entries, fileName)
                "pdf" -> exportManager.exportPdf(entries, fileName, "${state.fromDate} to ${state.toDate}")
                else -> Result.failure(Exception("Unknown format"))
            }
            
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(isExporting = false, exportSuccessMessage = msg)
            }
            .onFailure { err ->
                _uiState.value = _uiState.value.copy(isExporting = false, exportError = err.message ?: "Export failed")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(exportSuccessMessage = null, exportError = null)
    }
}
