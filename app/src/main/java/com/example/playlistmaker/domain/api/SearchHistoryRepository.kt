package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track

interface SearchHistoryRepository {

    fun saveTrack(track:Track)
    fun getHistory(): List<Track>
    fun clearHistory()
}