package com.example.smartledger.domain

import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.data.SmsRule
import com.example.smartledger.data.Transaction
import com.example.smartledger.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime

data class PendingMessage(
    val id: String,
    val channel: String,
    val text: String,
    val methodId: Long? = null,
    val cardId: Long? = null,
    val suggestedRule: SmsRule? = null
)

class MessageProcessor(
    private val repository: LedgerRepository,
    private val ruleEngine: RuleEngine
) {
    private val pending = MutableStateFlow<List<PendingMessage>>(emptyList())
    val pendingMessages: StateFlow<List<PendingMessage>> = pending

    suspend fun processIncoming(
        text: String,
        channel: String,
        methodId: Long? = null,
        cardId: Long? = null
    ) {
        val rules = repository.rules().firstOrNull().orEmpty()
        val matched = ruleEngine.match(rules, cardId, methodId, channel, text)
        if (matched != null) {
            val amount = ruleEngine.parseAmount(matched, text)
            val categoryId = matched.categoryId ?: 1L
            val method = matched.methodId ?: methodId ?: 1L
            if (amount != null) {
                repository.saveTransaction(
                    Transaction(
                        amount = amount,
                        type = matched.type,
                        categoryId = categoryId,
                        methodId = method,
                        cardId = cardId,
                        occurredAt = LocalDateTime.now(),
                        note = "短信/通知自动导入",
                        matchedRuleId = matched.id,
                        isFromSms = true,
                        source = channel
                    )
                )
                return
            }
        }
        // no match or no amount parsed -> add to pending
        val pendingItem = PendingMessage(
            id = "${System.currentTimeMillis()}_${text.hashCode()}",
            channel = channel,
            text = text,
            methodId = methodId,
            cardId = cardId,
            suggestedRule = matched
        )
        pending.value = pending.value + pendingItem
    }

    suspend fun saveRuleAndApply(
        pendingId: String,
        rule: SmsRule,
        type: TransactionType,
        amount: Double,
        categoryId: Long,
        methodId: Long,
        cardId: Long?
    ) {
        val newRuleId = repository.saveRule(rule)
        repository.saveTransaction(
            Transaction(
                amount = amount,
                type = type,
                categoryId = categoryId,
                methodId = methodId,
                cardId = cardId,
                occurredAt = LocalDateTime.now(),
                note = "手动确认记录",
                matchedRuleId = newRuleId,
                isFromSms = true,
                source = rule.channel
            )
        )
        pending.value = pending.value.filterNot { it.id == pendingId }
    }
}

