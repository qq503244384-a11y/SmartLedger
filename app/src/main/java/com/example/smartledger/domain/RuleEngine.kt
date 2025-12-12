package com.example.smartledger.domain

import com.example.smartledger.data.SmsRule
import com.example.smartledger.data.TransactionType

data class RuleMatchResult(
    val rule: SmsRule,
    val amount: Double?,
    val dateString: String?,
    val type: TransactionType
)

class RuleEngine {
    /**
        Match rules by priority: card > method > channel > global (priority desc inside).
        @param cardId target card id if known
        @param methodId target method id if known
        @param channel channel name (WeChat/Alipay/SMS/Other)
        @param text raw message/notification text
     */
    fun match(
        rules: List<SmsRule>,
        cardId: Long?,
        methodId: Long?,
        channel: String,
        text: String
    ): SmsRule? {
        val normalized = text.lowercase()
        val ordered = rules
            .filter { it.enabled }
            .sortedWith(
                compareByDescending<SmsRule> { priorityBucket(it, cardId, methodId, channel) }
                    .thenByDescending { it.priority }
            )
        return ordered.firstOrNull { rule ->
            val keywordList = rule.keywords.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            keywordList.all { normalized.contains(it) }
        }
    }

    fun parseAmount(rule: SmsRule, text: String): Double? {
        rule.regex?.let { pattern ->
            return runCatching {
                val regex = Regex(pattern)
                val match = regex.find(text) ?: return null
                val groupIndex = rule.amountGroup ?: 1
                match.groupValues.getOrNull(groupIndex)?.toDoubleOrNull()
            }.getOrNull()
        }
        return Regex("(\\d+\\.?\\d*)").find(text)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
    }

    private fun priorityBucket(rule: SmsRule, cardId: Long?, methodId: Long?, channel: String): Int {
        return when {
            rule.cardId != null && rule.cardId == cardId -> 4
            rule.methodId != null && rule.methodId == methodId -> 3
            rule.channel.equals(channel, ignoreCase = true) -> 2
            else -> 1
        }
    }
}

