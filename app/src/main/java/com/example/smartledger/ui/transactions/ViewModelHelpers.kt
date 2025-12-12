package com.example.smartledger.ui.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartledger.data.LedgerRepository

@Composable
fun rememberAddTransactionViewModel(repository: LedgerRepository): AddTransactionViewModel {
    val factory = remember(repository) { AddTransactionViewModelFactory(repository) }
    return viewModel(factory = factory)
}






