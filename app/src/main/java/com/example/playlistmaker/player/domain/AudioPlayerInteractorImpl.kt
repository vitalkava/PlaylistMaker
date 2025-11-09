package com.example.playlistmaker.player.domain

class AudioPlayerInteractorImpl(private val repository: AudioPlayerRepository) :
    AudioPlayerInteractor {
    override fun prepare(url: String?, onPrepared: () -> Unit, onComplete: () -> Unit) {
        if (url.isNullOrEmpty()) {
            onComplete()
            return
        }
        repository.prepare(url, onPrepared, onComplete)
    }

    override fun play() = repository.play()
    override fun pause() = repository.pause()
    override fun release() = repository.release()
    override fun isPlaying() = repository.isPlaying()
    override fun getCurrentPosition() = repository.getCurrentPosition()
}