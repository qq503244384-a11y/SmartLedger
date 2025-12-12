package com.example.smartledger.domain

import com.example.smartledger.data.Budget
import com.example.smartledger.data.Transaction
import com.example.smartledger.data.TransactionType
import java.time.LocalDate

data class BudgetStatus(
    val budget: Budget?,
    val spent: Double,
    val remaining: Double,
    val overspent: Boolean
)

object BudgetCalculator {
    fun compute(
        budgets: List<Budget>,
        transactions: List<Transaction>,
        month: LocalDate
    ): BudgetStatus {
        val monthBudget = budgets.firstOrNull { it.month == month }
        val monthTx = transactions.filter {
            it.occurredAt.year == month.year && it.occurredAt.month == month.month && it.type == TransactionType.Expense
        }
        val spent = monthTx.sumOf { it.amount }
        val budgetAmount = monthBudget?.amount ?: 0.0
        val remaining = budgetAmount - spent
        return BudgetStatus(
            budget = monthBudget,
            spent = spent,
            remaining = remaining,
            overspent = budgetAmount > 0 && spent > budgetAmount
        )
    }
}






