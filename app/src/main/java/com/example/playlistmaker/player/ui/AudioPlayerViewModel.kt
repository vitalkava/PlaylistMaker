package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.AudioPlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlayerState {
    PREPARING, PLAYING, PAUSED, COMPLETED
}

data class AudioPlayerScreenState(
    val playerState: PlayerState,
    val currentPosition: Int
)

class AudioPlayerViewModel(
    private val audioInteractor: AudioPlayerInteractor
) : ViewModel() {

    private var progressJob: Job? = null

    private val _screenState = MutableLiveData(AudioPlayerScreenState(PlayerState.PREPARING, 0))
    val screenState: LiveData<AudioPlayerScreenState> = _screenState

    fun prepare(url: String?) {

        if (url.isNullOrEmpty()) {
            _screenState.postValue(AudioPlayerScreenState(PlayerState.COMPLETED, 0))
            return
        }

        _screenState.postValue(AudioPlayerScreenState(PlayerState.PREPARING, 0))
        audioInteractor.prepare(url, {
            _screenState.postValue(AudioPlayerScreenState(PlayerState.PAUSED, 0))
        }, {
            _screenState.postValue(AudioPlayerScreenState(PlayerState.COMPLETED, 0))
            stopProgressUpdates()
        })
    }

    fun togglePlayPause() {
        when (_screenState.value?.playerState) {
            PlayerState.PLAYING -> {
                audioInteractor.pause()
                _screenState.postValue(
                    AudioPlayerScreenState(
                        PlayerState.PAUSED,
                        _screenState.value!!.currentPosition
                    )
                )
                stopProgressUpdates()
            }

            PlayerState.PAUSED -> {
                if (!audioInteractor.isPlaying()) {
                    audioInteractor.play()
                    _screenState.postValue(
                        AudioPlayerScreenState(
                            PlayerState.PLAYING,
                            _screenState.value!!.currentPosition
                        )
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
                    AudioPlayerScreenState(
                        PlayerState.PLAYING,
                        audioInteractor.getCurrentPosition()
                    )
                )
                delay(300)
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