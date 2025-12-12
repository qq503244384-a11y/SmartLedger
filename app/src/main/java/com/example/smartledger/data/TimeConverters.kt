package com.example.smartledger.data

import androidx.room.TypeConverter
import com.example.smartledger.data.TransactionType
import com.example.smartledger.data.BudgetScope
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimeConverters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val enumParser: (String) -> TransactionType = { value ->
        runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.Expense)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? =
        value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }

    @TypeConverter
    fun dateTimeToTimestamp(date: LocalDateTime?): Long? =
        date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun fromDateString(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, dateFormatter) }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? =
        date?.format(dateFormatter)

    @TypeConverter
    fun transactionTypeToString(type: TransactionType?): String? = type?.name

    @TypeConverter
    fun stringToTransactionType(value: String?): TransactionType? =
        value?.let(enumParser)

    @TypeConverter
    fun budgetScopeToString(scope: BudgetScope?): String? = scope?.name

    @TypeConverter
    fun stringToBudgetScope(value: String?): BudgetScope? =
        value?.let { runCatching { BudgetScope.valueOf(it) }.getOrDefault(BudgetScope.All) }
}

