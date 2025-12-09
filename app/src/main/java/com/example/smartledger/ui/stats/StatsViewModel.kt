package com.example.smartledger.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.data.Transaction
import com.example.smartledger.data.TransactionType
import com.example.smartledger.domain.BudgetCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.LocalDate

data class StatsUiState(
    val monthExpense: Double = 0.0,
    val monthIncome: Double = 0.0,
    val balance: Double = 0.0,
    val recent: List<Transaction> = emptyList(),
    val budgetAmount: Double = 0.0,
    val spent: Double = 0.0,
    val overspent: Boolean = false,
    val pieData: Map<String, Double> = emptyMap(),
    val methodPie: Map<String, Double> = emptyMap(),
    val trendPoints: List<Pair<Int, Double>> = emptyList(),
    val budgetInput: String = ""
)

class StatsViewModel(
    private val repository: LedgerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state

    init {
        viewModelScope.launch {
            repository.transactions().collectLatest { list ->
                val month = LocalDate.now().month
                val filtered = list.filter { it.occurredAt.month == month }
                val expense = filtered.filter { it.type == TransactionType.Expense }.sumOf { it.amount }
                val income = filtered.filter { it.type == TransactionType.Income }.sumOf { it.amount }
                val budgets = repository.budgets().first()
                val categories = (repository.categories(TransactionType.Expense).first() +
                        repository.categories(TransactionType.Income).first())
                    .associateBy { it.id }
                val methods = repository.methods().first().associateBy { it.id }
                val budgetStatus = BudgetCalculator.compute(budgets, list, LocalDate.now().withDayOfMonth(1))
                val pie = filtered
                    .filter { it.type == TransactionType.Expense }
                    .groupBy { categories[it.categoryId]?.name ?: it.categoryId.toString() }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                val methodPie = filtered
                    .filter { it.type == TransactionType.Expense }
                    .groupBy { methods[it.methodId]?.name ?: it.methodId.toString() }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                val trend = filtered
                    .filter { it.type == TransactionType.Expense }
                    .groupBy { it.occurredAt.dayOfMonth }
                    .mapValues { it.value.sumOf { tx -> tx.amount } }
                    .toSortedMap()
                    .map { it.key to it.value }
                _state.value = StatsUiState(
                    monthExpense = expense,
                    monthIncome = income,
                    balance = income - expense,
                    recent = list.take(5),
                    budgetAmount = budgetStatus.budget?.amount ?: 0.0,
                    spent = budgetStatus.spent,
                    overspent = budgetStatus.overspent,
                    pieData = pie,
                    methodPie = methodPie,
                    trendPoints = trend,
                    budgetInput = budgetStatus.budget?.amount?.toString() ?: ""
                )
            }
        }
    }

    fun setBudgetInput(value: String) {
        _state.value = _state.value.copy(budgetInput = value)
    }

    fun saveBudget() {
        viewModelScope.launch {
            val amount = _state.value.budgetInput.toDoubleOrNull() ?: return@launch
            val month = LocalDate.now().withDayOfMonth(1)
            repository.saveBudget(
                com.example.smartledger.data.Budget(
                    month = month,
                    amount = amount
                )
            )
        }
    }
}

class StatsViewModelFactory(
    private val repository: LedgerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

