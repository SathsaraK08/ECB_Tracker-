package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            authRepository.signIn(email, pass)
                .onSuccess { _uiState.value = LoginUiState(isSuccess = true) }
                .onFailure { _uiState.value = LoginUiState(error = it.message ?: "Sign in failed") }
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            authRepository.signUp(email, pass)
                .onSuccess { _uiState.value = LoginUiState(isSuccess = true) }
                .onFailure { _uiState.value = LoginUiState(error = it.message ?: "Sign up failed") }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
