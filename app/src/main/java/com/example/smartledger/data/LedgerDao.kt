package com.example.smartledger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(tx: Transaction): Long

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    fun streamTransactions(): Flow<List<Transaction>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun countTransactions(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<Category>)

    @Query("SELECT * FROM categories WHERE type = :type")
    fun streamCategories(type: TransactionType): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMethod(method: Method)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMethods(methods: List<Method>)

    @Query("SELECT * FROM methods")
    fun streamMethods(): Flow<List<Method>>

    @Query("SELECT COUNT(*) FROM methods")
    suspend fun countMethods(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCard(card: Card)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCards(cards: List<Card>)

    @Query("SELECT * FROM cards WHERE methodId = :methodId")
    fun streamCards(methodId: Long): Flow<List<Card>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: Budget)

    @Query("SELECT * FROM budgets")
    fun streamBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE month = :monthString LIMIT 1")
    suspend fun budgetForMonth(monthString: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: SmsRule): Long

    @Query("SELECT * FROM sms_rules ORDER BY priority DESC")
    fun streamRules(): Flow<List<SmsRule>>
}

