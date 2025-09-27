package com.example.playlistmaker.library.ui.favorites

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.Track

class FavoritesViewModel : ViewModel() {
    fun getFavorites(): List<Track> = emptyList() // plug
}