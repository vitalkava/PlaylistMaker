package com.example.playlistmaker.player.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityAudioPlayerBinding
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioPlayerBinding
    private val viewModel: AudioPlayerViewModel by viewModel()
    private val gson: Gson by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val trackJson = intent.getStringExtra("TRACK_DATA")
        val track = gson.fromJson(trackJson, Track::class.java)
        updateUI(track)

        viewModel.screenState.observe(this) { state ->
            binding.playButton.isEnabled = state.playerState != PlayerState.PREPARING
            binding.playButton.setImageResource(
                if (state.playerState == PlayerState.PLAYING) R.drawable.pause_button else R.drawable.play_button
            )
            binding.progressTrack.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(state.currentPosition)
        }

        binding.playButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        viewModel.prepare(track.previewUrl)
    }

    private fun updateUI(track: Track) {
        binding.tvSongName.text = track.trackName
        binding.tvBandName.text = track.artistName
        binding.tvSongDurationValue.text = track.trackTime
        binding.tvAlbumNameValue.text = track.collectionName
        binding.tvSongYearValue.text = track.releaseDate.take(4)
        binding.tvSongGenreValue.text = track.primaryGenreName
        binding.tvTrackCountryValue.text = track.country

        val artworkUrl = track.artworkUrl100.replaceAfterLast("/", "512x512bb.jpg")
        Glide.with(this)
            .load(artworkUrl)
            .placeholder(R.drawable.album_card_312dp)
            .into(binding.ivAlbumCard)

        binding.progressTrack.text = "00:00"
        binding.playButton.isEnabled = false
    }
}