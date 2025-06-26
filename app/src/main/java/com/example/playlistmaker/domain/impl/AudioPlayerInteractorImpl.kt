package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.AudioPlayerInteractor
import com.example.playlistmaker.domain.api.AudioPlayerRepository

class AudioPlayerInteractorImpl(private val repository: AudioPlayerRepository) :
    AudioPlayerInteractor {
    override fun prepare(url: String, onPrepared: () -> Unit, onComplete: () -> Unit) {
        repository.prepare(url, onPrepared, onComplete)
    }

    override fun play() = repository.play()
    override fun pause() = repository.pause()
    override fun release() = repository.release()
    override fun isPlaying() = repository.isPlaying()
    override fun getCurrentPosition() = repository.getCurrentPosition()
}