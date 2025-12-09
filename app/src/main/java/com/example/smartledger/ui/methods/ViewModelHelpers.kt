package com.example.smartledger.ui.methods

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartledger.data.LedgerRepository

@Composable
fun rememberMethodsViewModel(repository: LedgerRepository): MethodsViewModel {
    val factory = remember(repository) { MethodsViewModelFactory(repository) }
    return viewModel(factory = factory)
}


