package com.example.playlistmaker.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.creator.Creator

class PlayerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(c: Class<T>): T {
        if (c.isAssignableFrom(AudioPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioPlayerViewModel(Creator.provideAudioPlayerInteractor()) as T
        }
        throw IllegalArgumentException()
    }
}