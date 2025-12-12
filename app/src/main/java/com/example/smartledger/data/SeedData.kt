package com.example.smartledger.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

object SeedData {
    val defaultCategories = listOf(
        Category(name = "餐饮", type = TransactionType.Expense, icon = "food"),
        Category(name = "交通", type = TransactionType.Expense, icon = "transport"),
        Category(name = "购物", type = TransactionType.Expense, icon = "shopping"),
        Category(name = "住房", type = TransactionType.Expense, icon = "home"),
        Category(name = "娱乐", type = TransactionType.Expense, icon = "fun"),
        Category(name = "其他", type = TransactionType.Expense, icon = "other"),
        Category(name = "工资", type = TransactionType.Income, icon = "salary"),
        Category(name = "理财", type = TransactionType.Income, icon = "invest"),
        Category(name = "退款", type = TransactionType.Income, icon = "refund")
    )

    val defaultMethods = listOf(
        Method(name = "微信钱包", type = TransactionType.Expense, isCredit = false),
        Method(name = "支付宝", type = TransactionType.Expense, isCredit = false),
        Method(name = "支付宝花呗", type = TransactionType.Expense, isCredit = true, billDay = 5, dueDay = 15),
        Method(name = "银行卡", type = TransactionType.Expense, isCredit = false),
        Method(name = "信用卡", type = TransactionType.Expense, isCredit = true, billDay = 10, dueDay = 25)
    )

    suspend fun seedIfEmpty(dao: LedgerDao) = withContext(Dispatchers.IO) {
        if (dao.countCategories() == 0) {
            dao.upsertCategories(defaultCategories)
        }
        if (dao.countMethods() == 0) {
            dao.upsertMethods(defaultMethods)
        }
        if (dao.countTransactions() == 0) {
            val now = LocalDateTime.now()
            dao.upsertTransaction(
                Transaction(
                    amount = 36.5,
                    type = TransactionType.Expense,
                    categoryId = 1,
                    methodId = 1,
                    occurredAt = now.minusDays(1),
                    note = "午餐示例"
                )
            )
            dao.upsertTransaction(
                Transaction(
                    amount = 12000.0,
                    type = TransactionType.Income,
                    categoryId = 7,
                    methodId = 4,
                    occurredAt = now.minusDays(3),
                    note = "工资示例"
                )
            )
        }
    }
}






