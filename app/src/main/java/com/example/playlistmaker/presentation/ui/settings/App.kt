package com.example.playlistmaker.presentation.ui.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class App: Application() {

    private var darkTheme = false

    override fun onCreate() {
        super.onCreate()
        val defaultTheme = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        val preferences = getSharedPreferences(Constants.SETTINGS_KEY, MODE_PRIVATE)
        darkTheme = preferences.getBoolean("darkTheme", defaultTheme)
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        getSharedPreferences(Constants.SETTINGS_KEY, MODE_PRIVATE)
            .edit() {
                putBoolean("darkTheme", darkThemeEnabled)
            }
    }
}