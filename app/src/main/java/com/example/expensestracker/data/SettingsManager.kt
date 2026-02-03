package com.example.expensestracker.data

import android.content.Context

class SettingsManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("expenses_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENCY = "currency"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    fun getCurrency(): Currency {
        val saved = prefs.getString(KEY_CURRENCY, Currency.BAM.name)
        return Currency.valueOf(saved!!)
    }

    fun setCurrency(currency: Currency) {
        prefs.edit().putString(KEY_CURRENCY, currency.name).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
}
