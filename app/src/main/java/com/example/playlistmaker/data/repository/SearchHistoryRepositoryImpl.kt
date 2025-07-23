package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences): SearchHistoryRepository {

    private val gson = Gson()

    override fun saveTrack(track: Track) {
        val trackList = getHistory().toMutableList()

        trackList.removeAll { it.trackName == track.trackName }

        trackList.add(0, track)

        if (trackList.size > MAX_HISTORY_SIZE) {
            trackList.removeAt(trackList.size - 1)
        }

        sharedPreferences.edit()
            .putString(SEARCH_HISTORY_KEY, gson.toJson(trackList))
            .apply()
    }

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, "[]")
        return gson.fromJson(json, Array<Track>::class.java).toList()
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    companion object{
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }
}