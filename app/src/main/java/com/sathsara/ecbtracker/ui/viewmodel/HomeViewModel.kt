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
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val username: String = "User",
    val monthlyKwh: Double = 0.0,
    val estimatedBill: Double = 0.0,
    val todayKwh: Double = 0.0,
    val ratePerUnit: Double = 32.0,
    val chartData: List<Pair<String, Float>> = emptyList(),
    val recentActivity: List<Entry> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

        fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Get current user profile and settings
            val profile = settingsRepository.getProfile().getOrNull()
            val settings = settingsRepository.getSettings().getOrNull()
            
            val username = profile?.username?.takeIf { it.isNotBlank() } ?: "User"
            val rate = settings?.lkrPerUnit ?: 32.0
            
            // Generate current month string e.g. "2023-10"
            val currentMoment = LocalDateTime.now()
            val monthStr = currentMoment.monthValue.toString().padStart(2, '0')
            val yearMonth = "${currentMoment.year}-$monthStr"
            val todayStr = currentMoment.toLocalDate().toString()

            // Fetch entries
            val monthEntries = entryRepository.getEntriesForMonth(yearMonth).getOrNull() ?: emptyList()
            val recentEntries = entryRepository.getRecentEntries(3).getOrNull() ?: emptyList()
            
            // Calculate totals
            val monthlyKwh = monthEntries.sumOf { it.used }
            val todayKwh = monthEntries.filter { it.date == todayStr }.sumOf { it.used }
            
            // Generate chart data (last 7 days)
            val last7DaysMap = monthEntries
                .sortedByDescending { it.date }
                .take(7)
                .groupBy { it.date }
                .mapValues { (_, entries) -> entries.sumOf { it.used }.toFloat() }
                
            val chartData = last7DaysMap.entries.map { Pair(it.key, it.value) }.take(7)

            _uiState.value = HomeUiState(
                isLoading = false,
                username = username,
                monthlyKwh = monthlyKwh,
                estimatedBill = monthlyKwh * rate,
                todayKwh = todayKwh,
                ratePerUnit = rate,
                chartData = chartData,
                recentActivity = recentEntries
            )
        }
    }
}
