package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.model.Payment
import com.sathsara.ecbtracker.data.repository.EntryRepository
import com.sathsara.ecbtracker.data.repository.PaymentRepository
import com.sathsara.ecbtracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class PaymentsUiState(
    val isLoading: Boolean = true,
    val currentMonthLabel: String = "",
    val currentBillAmount: Double = 0.0,
    val lastUnits: Double = 0.0,
    val accountNumber: String = "",
    val paymentHistory: List<Payment> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val entryRepository: EntryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val currentMoment = LocalDateTime.now()
            val monthStr = currentMoment.monthValue.toString().padStart(2, '0')
            val yearMonthStr = "${currentMoment.year}-$monthStr"
            
            // Format friendly month label
            val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            val friendlyMonth = "${months[currentMoment.monthValue - 1]} ${currentMoment.year}"
            
            val settings = settingsRepository.getSettings().getOrNull()
            val rate = settings?.lkrPerUnit ?: 32.0
            val account = settings?.accountNumber ?: "No Account Set"

            // Get month usage
            val entriesThisMonth = entryRepository.getEntriesForMonth(yearMonthStr).getOrNull() ?: emptyList()
            val totalUsed = entriesThisMonth.sumOf { it.used }
            val estBill = totalUsed * rate
            
            // Get payments history
            val history = paymentRepository.getPayments().getOrNull() ?: emptyList()
            
            _uiState.value = PaymentsUiState(
                isLoading = false,
                currentMonthLabel = friendlyMonth,
                currentBillAmount = estBill,
                lastUnits = totalUsed,
                accountNumber = account,
                paymentHistory = history,
                error = null
            )
        }
    }
}
