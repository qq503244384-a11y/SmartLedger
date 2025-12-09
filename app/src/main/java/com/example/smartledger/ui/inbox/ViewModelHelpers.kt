package com.example.smartledger.ui.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.domain.MessageProcessor

@Composable
fun rememberInboxViewModel(
    processor: MessageProcessor,
    repository: LedgerRepository
): InboxViewModel {
    val factory = remember(processor, repository) { InboxViewModelFactory(processor, repository) }
    return viewModel(factory = factory)
}

