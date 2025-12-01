package com.example.playlistmaker.library.domain

import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistTrackRepository {
    suspend fun insertTrack(track: Track)
    fun getTracksByIds(trackIds: List<String>): Flow<List<Track>>
    suspend fun deleteTrack(trackId: String)
}