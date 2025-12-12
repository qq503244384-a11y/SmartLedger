package com.example.smartledger.ui.methods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.Card
import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.data.Method
import com.example.smartledger.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MethodsUiState(
    val methods: List<Method> = emptyList(),
    val cardsByMethod: Map<Long, List<Card>> = emptyMap()
)

class MethodsViewModel(
    private val repository: LedgerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MethodsUiState())
    val state: StateFlow<MethodsUiState> = _state

    init {
        viewModelScope.launch {
            repository.methods().collect { methods ->
                if (methods.isEmpty()) {
                    _state.value = MethodsUiState(emptyList(), emptyMap())
                    return@collect
                }
                val cardFlows = methods.map { repository.cards(it.id) }
                combine(cardFlows) { array ->
                    methods.zip(array.toList()) { m, cardsList -> m.id to cardsList }
                        .toMap()
                }.collect { cardsMap ->
                    _state.value = MethodsUiState(
                        methods = methods,
                        cardsByMethod = cardsMap
                    )
                }
            }
        }
    }

    fun addMethod(name: String, isCredit: Boolean, billDay: Int?, dueDay: Int?, repayLeadDays: Int?) {
        viewModelScope.launch {
            repository.saveMethod(
                Method(
                    name = name,
                    type = TransactionType.Expense,
                    isCredit = isCredit,
                    billDay = billDay,
                    dueDay = dueDay,
                    isCustom = true,
                    repayLeadDays = repayLeadDays
                )
            )
        }
    }

    fun addCard(methodId: Long, label: String, billDay: Int?, dueDay: Int?, repayLeadDays: Int?) {
        viewModelScope.launch {
            repository.saveCard(
                Card(
                    methodId = methodId,
                    label = label,
                    billDay = billDay,
                    dueDay = dueDay,
                    isCustom = true,
                    repayLeadDays = repayLeadDays
                )
            )
        }
    }
}

class MethodsViewModelFactory(
    private val repository: LedgerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MethodsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MethodsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

