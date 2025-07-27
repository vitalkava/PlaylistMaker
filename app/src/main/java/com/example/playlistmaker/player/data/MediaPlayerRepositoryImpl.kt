package com.example.playlistmaker.player.data

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.AudioPlayerRepository

class MediaPlayerRepositoryImpl(private val mediaPlayer: MediaPlayer) : AudioPlayerRepository {

    override fun prepare(url: String, onPrepared: () -> Unit, onComplete: () -> Unit) {
        mediaPlayer.apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { onPrepared() }
            setOnCompletionListener {
                onComplete()
                reset()
            }
        }
    }

    override fun play() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }
}