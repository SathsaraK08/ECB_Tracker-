package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.model.Profile
import com.sathsara.ecbtracker.data.model.UserSettings
import com.sathsara.ecbtracker.data.repository.AuthRepository
import com.sathsara.ecbtracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val settings: UserSettings? = null,
    val email: String = "",
    val error: String? = null,
    val isSignedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // DataStore states mapped as StateFlows
    val isDarkMode = dataStoreManager.isDarkMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val billReminders = dataStoreManager.billReminders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val usageAlerts = dataStoreManager.usageAlerts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val weeklyDigest = dataStoreManager.weeklyDigest.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val profile = settingsRepository.getProfile().getOrNull()
            val settings = settingsRepository.getSettings().getOrNull()
            val currentEmail = "" // In a real app we'd fetch this from Supabase Auth user object
            
            _uiState.value = SettingsUiState(
                isLoading = false,
                profile = profile,
                settings = settings,
                email = currentEmail
            )
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setDarkMode(enabled) }
    }

    fun toggleBillReminders(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setBillReminders(enabled) }
    }

    fun toggleUsageAlerts(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setUsageAlerts(enabled) }
    }

    fun toggleWeeklyDigest(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setWeeklyDigest(enabled) }
    }

    fun updateRate(newRate: Double) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings ?: UserSettings()
            val newSettings = currentSettings.copy(lkrPerUnit = newRate)
            settingsRepository.updateSettings(newSettings)
            loadData()
        }
    }

    fun updateAccountInfo(accountNumber: String?, ownerName: String?) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings ?: UserSettings()
            val newSettings = currentSettings.copy(
                accountNumber = accountNumber ?: currentSettings.accountNumber,
                ownerName = ownerName ?: currentSettings.ownerName
            )
            settingsRepository.updateSettings(newSettings)
            loadData()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = _uiState.value.copy(isSignedOut = true)
        }
    }
}
