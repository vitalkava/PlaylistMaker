package com.example.playlistmaker.presentation.ui.player

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.net.toUri
import com.example.playlistmaker.R
import com.example.playlistmaker.data.repository.MediaPlayerRepositoryImpl
import com.example.playlistmaker.domain.api.AudioPlayerInteractor
import com.example.playlistmaker.domain.impl.AudioPlayerInteractorImpl
import com.example.playlistmaker.domain.models.Track

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var interactor: AudioPlayerInteractor
    private lateinit var handler: Handler

    private lateinit var playButton: ImageView
    private lateinit var progressTrack: TextView
    private lateinit var artWork: ImageView

    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var songDuration: TextView
    private lateinit var albumName: TextView
    private lateinit var songYear: TextView
    private lateinit var songGenre: TextView
    private lateinit var songCountry: TextView

    private lateinit var track: Track

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (interactor.isPlaying()) {
                val position = interactor.getCurrentPosition()
                progressTrack.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(position)
                handler.postDelayed(this, 300)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)

        initViews()
        handler = Handler(Looper.getMainLooper())
        interactor = AudioPlayerInteractorImpl(MediaPlayerRepositoryImpl())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.button_back).setOnClickListener { finish() }

        intent.getStringExtra("TRACK_DATA")?.let {
            track = Gson().fromJson(it, Track::class.java)
            updateUI(track)
            preparePlayer(track)
        }

        playButton.setOnClickListener { togglePlayback() }
    }

    private fun initViews() {
        playButton = findViewById(R.id.play_button)
        progressTrack = findViewById(R.id.progress_track)
        artWork = findViewById(R.id.ivAlbumCard)

        trackName = findViewById(R.id.tvSongName)
        artistName = findViewById(R.id.tvBandName)
        songDuration = findViewById(R.id.tvSongDurationValue)
        albumName = findViewById(R.id.tvAlbumNameValue)
        songYear = findViewById(R.id.tvSongYearValue)
        songGenre = findViewById(R.id.tvSongGenreValue)
        songCountry = findViewById(R.id.tvTrackCountryValue)
    }

    private fun updateUI(track: Track) {
        progressTrack.text = "00:00"
        trackName.text = track.trackName
        artistName.text = track.artistName
        songDuration.text = track.trackTime
        albumName.text = track.collectionName.takeIf { it.isNotEmpty() } ?: run {
            albumName.visibility = View.GONE
            ""
        }
        songYear.text = track.releaseDate.take(4)
        songGenre.text = track.primaryGenreName
        songCountry.text = track.country

        val artworkUrl = track.artworkUrl100.replaceAfterLast("/", "512x512bb.jpg")
        Glide.with(this)
            .load(artworkUrl)
            .placeholder(R.drawable.album_card_312dp)
            .into(artWork)
    }

    private fun preparePlayer(track: Track) {
        interactor.prepare(
            url = track.previewUrl,
            onPrepared = {
                playButton.isEnabled = true
            },
            onComplete = {
                playButton.setImageResource(R.drawable.play_button)
                progressTrack.text = "00:00"
                stopUpdatingProgress()
            }
        )
    }

    private fun togglePlayback() {
        if (interactor.isPlaying()) {
            interactor.pause()
            playButton.setImageResource(R.drawable.play_button)
            stopUpdatingProgress()
        } else {
            interactor.play()
            playButton.setImageResource(R.drawable.pause_button)
            startUpdatingProgress()
        }
    }

    private fun startUpdatingProgress() {
        handler.post(updateRunnable)
    }

    private fun stopUpdatingProgress() {
        handler.removeCallbacks(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (interactor.isPlaying()) {
            interactor.pause()
            playButton.setImageResource(R.drawable.play_button)
        }
        stopUpdatingProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        interactor.release()
        stopUpdatingProgress()
    }
}