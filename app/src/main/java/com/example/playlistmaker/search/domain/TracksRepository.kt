package com.example.playlistmaker.search.domain

interface TracksRepository {
    fun searchTracks(expression: String): List<Track>
}