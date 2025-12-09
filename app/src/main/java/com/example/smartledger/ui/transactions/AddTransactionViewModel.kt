package com.example.smartledger.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.Card
import com.example.smartledger.data.Category
import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.data.Method
import com.example.smartledger.data.Transaction
import com.example.smartledger.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class AddTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.Expense,
    val categories: List<Category> = emptyList(),
    val methods: List<Method> = emptyList(),
    val cards: List<Card> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedMethodId: Long? = null,
    val selectedCardId: Long? = null,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val note: String = "",
    val saving: Boolean = false,
    val error: String? = null
)

class AddTransactionViewModel(
    private val repository: LedgerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionUiState())
    val state: StateFlow<AddTransactionUiState> = _state

    init {
        viewModelScope.launch {
            combine(
                repository.categories(TransactionType.Expense),
                repository.categories(TransactionType.Income),
                repository.methods()
            ) { expenseCats, incomeCats, methods ->
                val cats = if (_state.value.type == TransactionType.Expense) expenseCats else incomeCats
                _state.value.copy(
                    categories = cats,
                    methods = methods
                )
            }.collect { _state.value = it }
        }
        viewModelScope.launch {
            repository.methods().collect { methods ->
                val selectedMethod = _state.value.selectedMethodId
                if (selectedMethod == null && methods.isNotEmpty()) {
                    _state.value = _state.value.copy(selectedMethodId = methods.first().id)
                }
            }
        }
    }

    fun setAmount(value: String) {
        _state.value = _state.value.copy(amount = value, error = null)
    }

    fun setType(type: TransactionType) {
        _state.value = _state.value.copy(type = type, selectedCategoryId = null, error = null)
        viewModelScope.launch {
            val cats = repository.categories(type).first()
            _state.value = _state.value.copy(categories = cats)
        }
    }

    fun selectCategory(id: Long) {
        _state.value = _state.value.copy(selectedCategoryId = id)
    }

    fun selectMethod(id: Long) {
        _state.value = _state.value.copy(selectedMethodId = id, selectedCardId = null)
        viewModelScope.launch {
            repository.cards(id).collect { cards ->
                _state.value = _state.value.copy(cards = cards)
            }
        }
    }

    fun selectCard(id: Long?) {
        _state.value = _state.value.copy(selectedCardId = id)
    }

    fun setNote(note: String) {
        _state.value = _state.value.copy(note = note)
    }

    fun setDate(date: LocalDateTime) {
        _state.value = _state.value.copy(occurredAt = date)
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val amountValue = _state.value.amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                _state.value = _state.value.copy(error = "金额需大于0")
                return@launch
            }
            val categoryId = _state.value.selectedCategoryId ?: repository.categories(_state.value.type).first().firstOrNull()?.id
            val methodId = _state.value.selectedMethodId ?: repository.methods().first().firstOrNull()?.id
            if (categoryId == null || methodId == null) {
                _state.value = _state.value.copy(error = "请选择类别和方式")
                return@launch
            }
            _state.value = _state.value.copy(saving = true, error = null)
            repository.saveTransaction(
                Transaction(
                    amount = amountValue,
                    type = _state.value.type,
                    categoryId = categoryId,
                    methodId = methodId,
                    cardId = _state.value.selectedCardId,
                    occurredAt = _state.value.occurredAt,
                    note = _state.value.note
                )
            )
            _state.value = _state.value.copy(saving = false, amount = "", note = "")
            onSaved()
        }
    }
}

class AddTransactionViewModelFactory(
    private val repository: LedgerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

