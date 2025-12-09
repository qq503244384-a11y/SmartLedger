package com.example.smartledger.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SETTINGS_NAME = "ledger_settings"

val Context.appSettingsDataStore by preferencesDataStore(name = SETTINGS_NAME)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val language: Language = Language.System,
    val notificationListenerEnabled: Boolean = false,
    val repayLeadDays: Int = 3
)

enum class ThemeMode { Light, Dark, System }
enum class Language { ZhHans, En, ZhHant, System }

class SettingsRepository(private val context: Context) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTI = booleanPreferencesKey("notification_listener")
        val REPAY_LEAD = intPreferencesKey("repay_lead_days")
    }

    val settings: Flow<AppSettings> = context.appSettingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.System,
            language = prefs[Keys.LANGUAGE]?.let { runCatching { Language.valueOf(it) }.getOrNull() }
                ?: Language.System,
            notificationListenerEnabled = prefs[Keys.NOTI] ?: false,
            repayLeadDays = prefs[Keys.REPAY_LEAD] ?: 3
        )
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.appSettingsDataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setLanguage(language: Language) {
        context.appSettingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setNotificationListener(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.NOTI] = enabled }
    }

    suspend fun setRepayLeadDays(days: Int) {
        context.appSettingsDataStore.edit { it[Keys.REPAY_LEAD] = days }
    }
}

