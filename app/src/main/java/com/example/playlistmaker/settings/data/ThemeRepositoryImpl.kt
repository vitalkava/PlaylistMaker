package com.example.playlistmaker.settings.data

import android.content.SharedPreferences
import com.example.playlistmaker.settings.domain.ThemeRepository

class ThemeRepositoryImpl(private val sharedPreferences: SharedPreferences): ThemeRepository {

    override fun setTheme(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("darkTheme", isDarkMode).apply()
    }

    override fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean("darkTheme", false)
    }
}