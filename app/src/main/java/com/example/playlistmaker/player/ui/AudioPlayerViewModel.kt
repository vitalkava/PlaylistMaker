package com.example.playlistmaker.player.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.player.domain.AudioPlayerInteractor
import com.example.playlistmaker.settings.ui.HandlerFactory

enum class PlayerState {
    PREPARING, PLAYING, PAUSED, COMPLETED
}

data class AudioPlayerScreenState(
    val playerState: PlayerState,
    val currentPosition: Int
)

class AudioPlayerViewModel(
    private val audioInteractor: AudioPlayerInteractor,
    private val handlerFactory: HandlerFactory
) : ViewModel() {

    private val handler = handlerFactory.createMainHandler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (audioInteractor.isPlaying()) {
                _screenState.postValue(
                    AudioPlayerScreenState(
                        PlayerState.PLAYING,
                        audioInteractor.getCurrentPosition()
                    )
                )
                handler.postDelayed(this, 300)
            }
        }
    }

    private val _screenState = MutableLiveData(AudioPlayerScreenState(PlayerState.PREPARING, 0))
    val screenState: LiveData<AudioPlayerScreenState> = _screenState

    fun prepare(url: String) {
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
        handler.post(updateRunnable)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(updateRunnable)
    }

    override fun onCleared() {
        super.onCleared()
        audioInteractor.release()
        handler.removeCallbacksAndMessages(null)
    }
}