package com.example.smartledger.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Category::class,
        Method::class,
        Card::class,
        Transaction::class,
        Budget::class,
        CreditInstallment::class,
        SmsRule::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(TimeConverters::class)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
}

