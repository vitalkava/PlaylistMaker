package com.example.playlistmaker.domain.api

import com.example.playlistmaker.search.domain.Track

interface SearchHistoryRepository {

    fun saveTrack(track: Track)
    fun getHistory(): List<Track>
    fun clearHistory()
}