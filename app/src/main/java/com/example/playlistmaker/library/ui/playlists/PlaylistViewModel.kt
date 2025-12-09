package com.example.playlistmaker.library.ui.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.PlaylistInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class PlaylistState {
    object Loading : PlaylistState()
    data class Content(val playlist: Playlist, val tracks: List<Track>) : PlaylistState()
    object Empty : PlaylistState()
}

class PlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {
    private val _playlistState = MutableLiveData<PlaylistState>()
    val playlistState: LiveData<PlaylistState> = _playlistState

    fun loadPlaylist(playlistId: Long) {
        if (playlistId == 0L) {
            _playlistState.value = PlaylistState.Empty
            return
        }
        _playlistState.value = PlaylistState.Loading
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            if (playlist == null) {
                _playlistState.postValue(PlaylistState.Empty)
                return@launch
            }
            val tracks = if (playlist.trackIds.isEmpty()) {
                emptyList()
            } else {
                playlistInteractor.getTracksByIds(playlist.trackIds).first()
            }
            _playlistState.postValue(PlaylistState.Content(playlist, tracks))
        }
    }

    fun deleteTrackFromPlaylist(track: Track) {
        val current = (_playlistState.value as? PlaylistState.Content) ?: return
        viewModelScope.launch {
            playlistInteractor.removeTrackFromPlaylist(current.playlist.id, track.trackId)
            loadPlaylist(current.playlist.id)
        }
    }

    fun deletePlaylist(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(id)
            onComplete()
        }
    }
}