package com.example.playlistmaker.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.player.domain.AudioPlayerInteractor

class PlayerViewModelFactory(
    private val interactor: AudioPlayerInteractor
) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(c: Class<T>): T {
        if (c.isAssignableFrom(AudioPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioPlayerViewModel(interactor) as T
        }
        throw IllegalArgumentException()
    }
}
