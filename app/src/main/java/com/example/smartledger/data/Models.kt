package com.example.smartledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

enum class TransactionType { Income, Expense }

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val icon: String = "",
    val isCustom: Boolean = false
)

@Entity(tableName = "methods")
data class Method(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType = TransactionType.Expense,
    val isCredit: Boolean = false,
    val billDay: Int? = null,
    val dueDay: Int? = null,
    val totalLimit: Double? = null,
    val remainingLimit: Double? = null,
    val isCustom: Boolean = false,
    val repayLeadDays: Int? = null
)

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val methodId: Long,
    val label: String,
    val billDay: Int? = null,
    val dueDay: Int? = null,
    val totalLimit: Double? = null,
    val remainingLimit: Double? = null,
    val isCustom: Boolean = false,
    val repayLeadDays: Int? = null
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val methodId: Long,
    val cardId: Long? = null,
    val occurredAt: LocalDateTime,
    val note: String = "",
    val creditInstallmentId: Long? = null,
    val source: String = "Manual",
    val matchedRuleId: Long? = null,
    val isFromSms: Boolean = false
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: LocalDate,
    val amount: Double,
    val scope: BudgetScope = BudgetScope.All,
    val categoryId: Long? = null,
    val methodId: Long? = null,
    val cardId: Long? = null
)

enum class BudgetScope { All, Category, Method, Card }

@Entity(tableName = "credit_installments")
data class CreditInstallment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long?,
    val methodId: Long,
    val totalAmount: Double,
    val periods: Int,
    val paidPeriods: Int = 0,
    val nextDueDate: LocalDate,
    val minDue: Double? = null,
    val isClosed: Boolean = false
)

@Entity(tableName = "sms_rules")
data class SmsRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val channel: String,
    val methodId: Long? = null,
    val cardId: Long? = null,
    val keywords: String,
    val regex: String? = null,
    val amountGroup: Int? = null,
    val dateGroup: Int? = null,
    val type: TransactionType,
    val categoryId: Long? = null,
    val enabled: Boolean = true,
    val priority: Int = 0
)

