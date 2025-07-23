package com.example.playlistmaker.settings.domain

interface ThemeRepository {
    fun setTheme(isDarkMode: Boolean)
    fun isDarkTheme(): Boolean
}