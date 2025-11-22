package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.domain.Track
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

object TrackMapper {
    private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    fun mapToDomain(trackDto: TrackDto): Track {
        return Track(
            trackId = trackDto.trackId,
            trackName = trackDto.trackName,
            artistName = trackDto.artistName,
            trackTime = formatDuration(trackDto.trackTimeMillis),
            artworkUrl100 = trackDto.artworkUrl100,
            collectionName = trackDto.collectionName,
            releaseDate = trackDto.releaseDate,
            primaryGenreName = trackDto.primaryGenreName,
            country = trackDto.country,
            previewUrl = trackDto.previewUrl,
            isFavorite = false,

        )
    }

    private fun formatDuration(durationMs: Long): String {
        return timeFormat.format(Date(durationMs))
    }
}