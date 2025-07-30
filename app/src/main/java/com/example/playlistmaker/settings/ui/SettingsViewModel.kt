package com.example.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.GetCurrentThemeUseCase
import com.example.playlistmaker.settings.domain.SwitchThemeUseCase

class SettingsViewModel(
    private val getCurrentThemeUseCase: GetCurrentThemeUseCase,
    private val switchThemeUseCase: SwitchThemeUseCase
) : ViewModel() {

    private val _isDarkTheme = MutableLiveData<Boolean>()
    val isDarkTheme: LiveData<Boolean> = _isDarkTheme

    init {
        _isDarkTheme.value = getCurrentThemeUseCase.execute()
    }

    fun toggleTheme(isDark: Boolean) {
        switchThemeUseCase.execute(isDark)
        _isDarkTheme.value = isDark
    }
}