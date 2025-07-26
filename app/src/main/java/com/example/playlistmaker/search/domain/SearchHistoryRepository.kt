package com.example.playlistmaker.search.domain

interface SearchHistoryRepository {

    fun saveTrack(track: Track)
    fun getHistory(): List<Track>
    fun clearHistory()
}