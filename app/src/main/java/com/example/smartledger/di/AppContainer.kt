package com.example.smartledger.di

import android.content.Context
import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.data.SettingsRepository
import com.example.smartledger.domain.MessageProcessor
import com.example.smartledger.domain.RuleEngine
import com.example.smartledger.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val repository: LedgerRepository = LedgerRepository.build(context)
    val settingsRepository: SettingsRepository = SettingsRepository(context)
    val ruleEngine: RuleEngine = RuleEngine()
    val messageProcessor: MessageProcessor = MessageProcessor(repository, ruleEngine)

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        appScope.launch { repository.seed() }
        appScope.launch { NotificationHelper.ensureChannel(context) }
    }
}

