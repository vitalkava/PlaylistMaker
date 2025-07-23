package com.example.playlistmaker.player.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.player.domain.AudioPlayerInteractor

class AudioPlayerViewModel(
    private val audioInteractor: AudioPlayerInteractor
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (audioInteractor.isPlaying()) {
                _currentPosition.postValue(audioInteractor.getCurrentPosition())
                handler.postDelayed(this, 300)
            }
        }
    }

    private val _isPrepared = MutableLiveData(false)
    val isPrepared: LiveData<Boolean> = _isPrepared

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData(0)
    val currentPosition: LiveData<Int> = _currentPosition

    fun prepare(url: String) {
        audioInteractor.prepare(url, {
            _isPrepared.postValue(true)
        }, {
            _isPlaying.postValue(false)
            _currentPosition.postValue(0)
            stopProgressUpdates()
        })
    }

    fun togglePlayPause() {
        if (audioInteractor.isPlaying()) {
            audioInteractor.pause()
            _isPlaying.postValue(false)
            stopProgressUpdates()
        } else {
            audioInteractor.play()
            _isPlaying.postValue(true)
            startProgressUpdates()
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
