package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.FavoritesInteractor
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

class AudioPlayerViewModel(
    private val audioInteractor: AudioPlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor,

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

    override fun onCleared() {
        super.onCleared()
        audioInteractor.release()
        stopProgressUpdates()
    }
}