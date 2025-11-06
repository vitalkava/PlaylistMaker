package com.example.playlistmaker.player.data

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.AudioPlayerRepository

class MediaPlayerRepositoryImpl(
    private val mediaPlayerFactory: MediaPlayerFactory
) : AudioPlayerRepository {
    private var mediaPlayer: MediaPlayer? = null

    override fun prepare(url: String?, onPrepared: () -> Unit, onComplete: () -> Unit) {
        if (url.isNullOrEmpty()){
            mediaPlayer?.release()
            mediaPlayer = null
            onComplete()
            return
        }
        try {
            mediaPlayer?.release()
            mediaPlayer = mediaPlayerFactory.create().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { onPrepared() }
                setOnCompletionListener {
                    onComplete()
                    reset()
                }
                setOnErrorListener { _, what, extra ->
                    onComplete()
                    reset()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer?.release()
            mediaPlayer = null
            onComplete()
        }
    }

    override fun play() {
        mediaPlayer?.takeIf { !it.isPlaying }?.start()
    }

    override fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
}