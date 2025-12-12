package com.example.smartledger.ui.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartledger.data.LedgerRepository

@Composable
fun rememberStatsViewModel(repository: LedgerRepository): StatsViewModel {
    val factory = remember(repository) { StatsViewModelFactory(repository) }
    return viewModel(factory = factory)
}






