package com.example.playlistmaker.library.data.converters

import com.example.playlistmaker.library.data.db.entity.TrackEntity
import com.example.playlistmaker.search.domain.Track

class TrackDbConvertor {

    fun map(track: Track): TrackEntity {
        return TrackEntity(
            id = track.trackId,
            artworkUrl100 = track.artworkUrl100,
            trackName = track.trackName,
            artistName = track.artistName,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            trackTimeMillis = track.trackTime,
            previewUrl = track.previewUrl ?: "",
        )
    }

    fun map(track: TrackEntity): Track {
        return Track(
            trackId = track.id,
            artworkUrl100 = track.artworkUrl100,
            trackName = track.trackName,
            artistName = track.artistName,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            trackTime = track.trackTimeMillis,
            previewUrl = track.previewUrl.ifEmpty { null },
            isFavorite = true,
        )
    }
}