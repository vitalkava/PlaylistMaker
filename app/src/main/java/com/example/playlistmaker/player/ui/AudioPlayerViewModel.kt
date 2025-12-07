package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.FavoritesInteractor
import com.example.playlistmaker.library.domain.PlaylistInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.player.domain.AudioPlayerInteractor
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlayerState {
    PREPARING, PLAYING, PAUSED, COMPLETED
}

data class AudioPlayerScreenState(
    val playerState: PlayerState,
    val currentPosition: Int,
    val isFavorite: Boolean = false,
)

sealed class AddStatus {
    data class Success(val playlistName: String) : AddStatus()
    data class AlreadyExists(val playlistName: String) : AddStatus()
}

class AudioPlayerViewModel(
    private val audioInteractor: AudioPlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }

    private var progressJob: Job? = null

    private val _screenState = MutableLiveData(
        AudioPlayerScreenState(PlayerState.PREPARING, 0, false)
    )
    val screenState: LiveData<AudioPlayerScreenState> = _screenState

    private var currentTrack: Track? = null

    private val _playlistsForBottomSheet = MutableLiveData<List<Playlist>>()
    val playlistsForBottomSheet: LiveData<List<Playlist>> = _playlistsForBottomSheet
    private val _addToPlaylistStatus = MutableLiveData<AddStatus>()
    val addToPlaylistStatus: LiveData<AddStatus> = _addToPlaylistStatus

    fun setCurrentTrack(track: Track) {
        currentTrack = track
        loadFavoriteStatus()
    }

    private fun loadFavoriteStatus() {
        currentTrack?.let { track ->
            viewModelScope.launch {
                val favoriteIds = favoritesInteractor.getFavoritesIds()
                val isFavorite = track.trackId in favoriteIds
                track.isFavorite = isFavorite
                _screenState.postValue(_screenState.value?.copy(isFavorite = isFavorite))
            }
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                if (track.isFavorite) {
                    favoritesInteractor.removeFromFavorites(track)
                } else {
                    favoritesInteractor.addToFavorites(track)
                }
                track.isFavorite = !track.isFavorite
                _screenState.postValue(_screenState.value?.copy(isFavorite = track.isFavorite))
            }
        }
    }

    fun prepare(url: String?) {

        if (url.isNullOrEmpty()) {
            _screenState.postValue(
                AudioPlayerScreenState(
                    PlayerState.COMPLETED,
                    0,
                    currentTrack?.isFavorite ?: false
                )
            )
            return
        }

        _screenState.postValue(
            AudioPlayerScreenState(
                PlayerState.PREPARING,
                0,
                currentTrack?.isFavorite ?: false
            )
        )
        audioInteractor.prepare(url, {
            _screenState.postValue(
                AudioPlayerScreenState(
                    PlayerState.PAUSED,
                    0,
                    currentTrack?.isFavorite ?: false
                )
            )
        }, {
            _screenState.postValue(
                AudioPlayerScreenState(
                    PlayerState.COMPLETED,
                    0,
                    currentTrack?.isFavorite ?: false
                )
            )
            stopProgressUpdates()
        })
    }

    fun togglePlayPause() {
        when (_screenState.value?.playerState) {
            PlayerState.PLAYING -> {
                audioInteractor.pause()
                _screenState.postValue(
                    _screenState.value?.copy(playerState = PlayerState.PAUSED)
                )
                stopProgressUpdates()
            }

            PlayerState.PAUSED -> {
                if (!audioInteractor.isPlaying()) {
                    audioInteractor.play()
                    _screenState.postValue(
                        _screenState.value?.copy(playerState = PlayerState.PLAYING)
                    )
                    startProgressUpdates()
                }
            }
            else -> Unit
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()

        progressJob = viewModelScope.launch {
            while (isActive && audioInteractor.isPlaying()) {
                _screenState.postValue(
                    _screenState.value?.copy(
                        playerState = PlayerState.PLAYING,
                        currentPosition = audioInteractor.getCurrentPosition()
                    )
                )
                delay(PROGRESS_UPDATE_DELAY)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun loadPlaylistsForBottomSheet() {
        viewModelScope.launch {
            playlistInteractor.getPlaylists().collect {
                _playlistsForBottomSheet.postValue(it)
            }
        }
    }

    fun addToPlaylist(playlist: Playlist) {
        val track = currentTrack ?: return
        if (playlist.trackIds.contains(track.trackId)) {
            _addToPlaylistStatus.postValue(AddStatus.AlreadyExists(playlist.name))
        } else {
            viewModelScope.launch {
                playlistInteractor.addTrackToPlaylist(playlist, track)
                _addToPlaylistStatus.postValue(AddStatus.Success(playlist.name))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioInteractor.release()
        stopProgressUpdates()
    }
}