package com.example.smartledger

import android.app.Application
import com.example.smartledger.di.AppContainer

class SmartLedgerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        com.example.smartledger.notifications.ReminderScheduler.scheduleDaily(this)
    }
}

