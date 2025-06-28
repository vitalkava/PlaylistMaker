package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.ThemeRepository

class ThemeRepositoryImpl(private val sharedPreferences: SharedPreferences): ThemeRepository {

    override fun setTheme(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("darkTheme", isDarkMode).apply()
    }

    override fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean("darkTheme", false)
    }
}