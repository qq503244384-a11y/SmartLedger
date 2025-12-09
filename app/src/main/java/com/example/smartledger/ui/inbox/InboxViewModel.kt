package com.example.smartledger.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.data.TransactionType
import com.example.smartledger.domain.MessageProcessor
import com.example.smartledger.domain.PendingMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class InboxUiState(
    val pending: List<PendingMessage> = emptyList(),
    val expenseCategories: List<com.example.smartledger.data.Category> = emptyList(),
    val incomeCategories: List<com.example.smartledger.data.Category> = emptyList(),
    val methods: List<com.example.smartledger.data.Method> = emptyList(),
    val cardsByMethod: Map<Long, List<com.example.smartledger.data.Card>> = emptyMap(),
    val messageAmount: Map<String, String> = emptyMap(),
    val messageKeywords: Map<String, String> = emptyMap(),
    val selectedType: Map<String, TransactionType> = emptyMap(),
    val selectedCategory: Map<String, Long> = emptyMap(),
    val selectedMethod: Map<String, Long> = emptyMap(),
    val selectedCard: Map<String, Long?> = emptyMap(),
    val savingIds: Set<String> = emptySet(),
    val error: String? = null
)

class InboxViewModel(
    private val processor: MessageProcessor,
    private val repository: LedgerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InboxUiState())
    val state: StateFlow<InboxUiState> = _state

    init {
        viewModelScope.launch {
            combine(
                processor.pendingMessages,
                repository.categories(TransactionType.Expense),
                repository.categories(TransactionType.Income),
                repository.methods()
            ) { pending, exp, inc, methods ->
                Quad(pending, exp, inc, methods)
            }.collectLatest { (pending, exp, inc, methods) ->
                if (methods.isEmpty()) {
                    _state.value = _state.value.copy(
                        pending = pending,
                        expenseCategories = exp,
                        incomeCategories = inc,
                        methods = methods,
                        cardsByMethod = emptyMap(),
                        messageAmount = presetAmounts(pending),
                        messageKeywords = presetKeywords(pending)
                    )
                } else {
                    val cardsMap = methods.associate { method -> method.id to repository.cards(method.id) }
                    val cardsCombined = combine(cardsMap.values.toList()) { arr ->
                        methods.zip(arr.toList()) { m, cards -> m.id to cards }.toMap()
                    }
                    cardsCombined.collectLatest { map ->
                        _state.value = _state.value.copy(
                            pending = pending,
                            expenseCategories = exp,
                            incomeCategories = inc,
                            methods = methods,
                            cardsByMethod = map,
                            messageAmount = presetAmounts(pending),
                            messageKeywords = presetKeywords(pending)
                        )
                    }
                }
            }
        }
    }

    private fun presetAmounts(pending: List<PendingMessage>): Map<String, String> =
        pending.associate { msg ->
            val num = Regex("(\\d+\\.?\\d*)").find(msg.text)?.groupValues?.getOrNull(1)
            msg.id to (num ?: (_state.value.messageAmount[msg.id] ?: ""))
        }

    private fun presetKeywords(pending: List<PendingMessage>): Map<String, String> =
        pending.associate { msg ->
            msg.id to (_state.value.messageKeywords[msg.id] ?: deriveKeywords(msg.text))
        }

    fun setAmount(id: String, amount: String) {
        _state.value = _state.value.copy(
            messageAmount = _state.value.messageAmount + (id to amount)
        )
    }

    fun setKeywords(id: String, keywords: String) {
        _state.value = _state.value.copy(
            messageKeywords = _state.value.messageKeywords + (id to keywords)
        )
    }

    fun setType(id: String, type: TransactionType) {
        _state.value = _state.value.copy(
            selectedType = _state.value.selectedType + (id to type)
        )
    }

    fun setCategory(id: String, categoryId: Long) {
        _state.value = _state.value.copy(
            selectedCategory = _state.value.selectedCategory + (id to categoryId)
        )
    }

    fun setMethod(id: String, methodId: Long) {
        _state.value = _state.value.copy(
            selectedMethod = _state.value.selectedMethod + (id to methodId),
            selectedCard = _state.value.selectedCard + (id to null)
        )
    }

    fun setCard(id: String, cardId: Long?) {
        _state.value = _state.value.copy(
            selectedCard = _state.value.selectedCard + (id to cardId)
        )
    }

    fun confirm(
        pending: PendingMessage,
        fallbackType: TransactionType,
        fallbackCategory: Long,
        fallbackMethod: Long,
        fallbackCard: Long?
    ) {
        val amountStr = _state.value.messageAmount[pending.id] ?: ""
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.value = _state.value.copy(error = "金额需大于0")
            return
        }
        val type = _state.value.selectedType[pending.id] ?: fallbackType
        val categoryId = _state.value.selectedCategory[pending.id] ?: fallbackCategory
        val methodId = _state.value.selectedMethod[pending.id] ?: fallbackMethod
        val cardId = _state.value.selectedCard[pending.id] ?: fallbackCard
        val keywords = (_state.value.messageKeywords[pending.id] ?: deriveKeywords(pending.text))
            .ifBlank { deriveKeywords(pending.text) }

        val rule = com.example.smartledger.data.SmsRule(
            name = "规则-${pending.channel}",
            channel = pending.channel,
            methodId = methodId,
            cardId = cardId,
            keywords = keywords,
            regex = null,
            amountGroup = null,
            dateGroup = null,
            type = type,
            categoryId = categoryId,
            enabled = true,
            priority = 10
        )

        viewModelScope.launch {
            _state.value = _state.value.copy(savingIds = _state.value.savingIds + pending.id, error = null)
            processor.saveRuleAndApply(
                pendingId = pending.id,
                rule = rule,
                type = type,
                amount = amount,
                categoryId = categoryId,
                methodId = methodId,
                cardId = cardId
            )
            _state.value = _state.value.copy(savingIds = _state.value.savingIds - pending.id)
        }
    }

    private fun deriveKeywords(text: String): String {
        return text.split(" ")
            .filter { it.any(Char::isLetterOrDigit) }
            .take(3)
            .joinToString(separator = ",")
            .lowercase()
    }
}

class InboxViewModelFactory(
    private val processor: MessageProcessor,
    private val repository: LedgerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InboxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InboxViewModel(processor, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

