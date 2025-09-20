package com.example.playlistmaker.library.ui.playlists

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.Track

class PlaylistsViewModel : ViewModel() {
    fun getPlaylists(): List<Track> = emptyList()
}