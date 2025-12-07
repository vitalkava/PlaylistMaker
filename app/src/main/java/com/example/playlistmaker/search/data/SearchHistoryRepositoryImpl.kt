package com.example.playlistmaker.search.data

import android.content.SharedPreferences
import com.example.playlistmaker.library.data.db.dao.TrackDao
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val trackDao: TrackDao,
) : SearchHistoryRepository, KoinComponent {

    private val gson: Gson by inject()

    override suspend fun saveTrack(track: Track) {
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

    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, "[]")
        val tracks = gson.fromJson(json, Array<Track>::class.java).toList()
        val favoriteIds = trackDao.getId()
        tracks.forEach { track -> track.isFavorite = track.trackId in favoriteIds }
        return tracks
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }
}