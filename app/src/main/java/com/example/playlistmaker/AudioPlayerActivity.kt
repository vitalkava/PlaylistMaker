package com.example.playlistmaker

import android.os.Bundle
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

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var buttonBack: Button
    private lateinit var playButton: ImageView
    private lateinit var addToPlayListButton: ImageView
    private lateinit var addToFavoritesButton: ImageView

    private lateinit var artWork: ImageView

    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var progressTrack: TextView
    private lateinit var songDuration: TextView
    private lateinit var albumName: TextView
    private lateinit var songYear: TextView
    private lateinit var songGenre: TextView
    private lateinit var songCountry: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)

        buttonBack = findViewById(R.id.button_back)
//        playButton = findViewById(R.id.play_button)
//        addToPlayListButton = findViewById(R.id.add_to_playlist_button)
//        addToFavoritesButton = findViewById(R.id.add_to_favorites_button)

        artWork = findViewById(R.id.ivAlbumCard)

        trackName = findViewById(R.id.tvSongName)
        artistName = findViewById(R.id.tvBandName)
        progressTrack = findViewById(R.id.progress_track)
        songDuration = findViewById(R.id.tvSongDurationValue)
        albumName = findViewById(R.id.tvAlbumNameValue)
        songYear = findViewById(R.id.tvSongYearValue)
        songGenre = findViewById(R.id.tvSongGenreValue)
        songCountry = findViewById(R.id.tvTrackCountryValue)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonBack.setOnClickListener {
            finish()
        }

        val jsonTrack = intent.getStringExtra("TRACK_DATA")
        jsonTrack?.let {
            val track = Gson().fromJson(it, Track::class.java)
            updateUI(track)
        }

    }

    private fun updateUI(track: Track) {
        progressTrack.text = "5:30"
        trackName.text = track.trackName
        artistName.text = track.artistName
        songDuration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        if (track.collectionName.isNotEmpty()) {
            albumName.text = track.collectionName
        } else {
            albumName.visibility = View.GONE
        }
        songYear.text = track.releaseDate.substring(0, 4) ?: "—"
        songGenre.text = track.primaryGenreName
        songCountry.text = track.country

        val artworkUrl = track.artworkUrl100.replaceAfterLast("/", "512x512bb.jpg") ?: ""
        if (artworkUrl.isNotEmpty()) {
            Glide.with(this)
                .load(artworkUrl)
                .placeholder(R.drawable.album_card_312dp)
                .into(artWork)
        } else {
            artWork.setImageResource(R.drawable.album_card_312dp)
        }
    }
}