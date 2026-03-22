package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.model.ForecastResponse
import com.sathsara.ecbtracker.data.repository.ForecastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class ForecastUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val forecast: ForecastResponse? = null
)

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val forecastRepository: ForecastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    init {
        loadForecast()
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun loadForecast() {
        viewModelScope.launch {
            _uiState.value = ForecastUiState(isLoading = true)
            
            val currentMoment = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val monthStr = currentMoment.monthNumber.toString().padStart(2, '0')
            val yearMonthStr = "${currentMoment.year}-$monthStr"

            val result = forecastRepository.getMonthlyForecast(yearMonthStr)
            
            result.onSuccess { response ->
                _uiState.value = ForecastUiState(
                    isLoading = false,
                    forecast = response
                )
            }.onFailure { err ->
                _uiState.value = ForecastUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = err.message ?: "Failed to generate AI forecast"
                )
            }
        }
    }
}
