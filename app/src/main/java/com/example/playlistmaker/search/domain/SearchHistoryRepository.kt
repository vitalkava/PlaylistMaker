package com.example.playlistmaker.search.domain

interface SearchHistoryRepository {

    suspend fun saveTrack(track: Track)
    suspend fun getHistory(): List<Track>
    fun clearHistory()
}