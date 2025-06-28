package com.example.playlistmaker.domain.api

interface AudioPlayerInteractor {
    fun prepare(url: String, onPrepared: () -> Unit, onComplete: () -> Unit)
    fun play()
    fun pause()
    fun release()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Int
}