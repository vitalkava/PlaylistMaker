package com.example.playlistmaker.search.domain

interface TracksInteractor {
    suspend fun searchTracks(expression: String): List<Track>
}