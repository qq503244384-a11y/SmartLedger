package com.example.smartledger.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.AppSettings
import com.example.smartledger.data.Language
import com.example.smartledger.data.SettingsRepository
import com.example.smartledger.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.System,
    val language: Language = Language.System,
    val notificationListenerEnabled: Boolean = false,
    val repayLeadDays: Int = 3
)

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _state.value = _state.value.copy(
                    themeMode = settings.themeMode,
                    language = settings.language,
                    notificationListenerEnabled = settings.notificationListenerEnabled,
                    repayLeadDays = settings.repayLeadDays
                )
            }
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { repository.setTheme(mode) }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun toggleNotificationListener(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationListener(enabled) }
    }

    fun setRepayLeadDays(days: Int) {
        val safe = days.coerceIn(0, 15)
        _state.value = _state.value.copy(repayLeadDays = safe)
        viewModelScope.launch { repository.setRepayLeadDays(safe) }
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

