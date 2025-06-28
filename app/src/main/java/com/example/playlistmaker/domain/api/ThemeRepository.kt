package com.example.playlistmaker.domain.api

interface ThemeRepository {
    fun setTheme(isDarkMode: Boolean)
    fun isDarkTheme(): Boolean
}