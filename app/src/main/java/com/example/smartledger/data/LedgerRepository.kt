package com.example.smartledger.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow

class LedgerRepository(private val dao: LedgerDao) {

    fun transactions(): Flow<List<Transaction>> = dao.streamTransactions()
    fun categories(type: TransactionType): Flow<List<Category>> = dao.streamCategories(type)
    fun methods(): Flow<List<Method>> = dao.streamMethods()
    fun cards(methodId: Long): Flow<List<Card>> = dao.streamCards(methodId)
    fun budgets(): Flow<List<Budget>> = dao.streamBudgets()
    suspend fun budgetForMonth(monthString: String): Budget? = dao.budgetForMonth(monthString)
    fun rules(): Flow<List<SmsRule>> = dao.streamRules()

    suspend fun saveTransaction(tx: Transaction): Long = dao.upsertTransaction(tx)
    suspend fun saveCategory(category: Category) = dao.upsertCategory(category)
    suspend fun saveMethod(method: Method) = dao.upsertMethod(method)
    suspend fun saveCard(card: Card) = dao.upsertCard(card)
    suspend fun saveBudget(budget: Budget) = dao.upsertBudget(budget)
    suspend fun saveRule(rule: SmsRule): Long = dao.upsertRule(rule)

    suspend fun seed() {
        SeedData.seedIfEmpty(dao)
    }

    companion object {
        fun build(context: Context): LedgerRepository {
            val db = Room.databaseBuilder(
                context,
                LedgerDatabase::class.java,
                "ledger.db"
            ).fallbackToDestructiveMigration().build()
            return LedgerRepository(db.ledgerDao())
        }
    }
}

