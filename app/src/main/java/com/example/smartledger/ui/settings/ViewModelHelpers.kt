package com.example.smartledger.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartledger.data.SettingsRepository

@Composable
fun rememberSettingsViewModel(repository: SettingsRepository): SettingsViewModel {
    val factory = remember(repository) { SettingsViewModelFactory(repository) }
    return viewModel(factory = factory)
}






