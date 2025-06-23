package com.example.playlistmaker.domain.use_case

import com.example.playlistmaker.domain.api.ThemeRepository

class GetCurrentThemeUseCase(private val repository: ThemeRepository) {

    fun execute():Boolean {
        return repository.isDarkTheme()
    }
}