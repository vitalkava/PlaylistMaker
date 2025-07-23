package com.example.playlistmaker.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.settings.domain.GetCurrentThemeUseCase
import com.example.playlistmaker.settings.domain.SwitchThemeUseCase

class SettingsViewModelFactory(
    private val getCurrentThemeUseCase: GetCurrentThemeUseCase,
    private val switchThemeUseCase: SwitchThemeUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(getCurrentThemeUseCase, switchThemeUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
