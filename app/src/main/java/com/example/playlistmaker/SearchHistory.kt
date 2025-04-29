package com.example.playlistmaker

import android.content.Context
import com.google.gson.Gson

class SearchHistory(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTrack(track: Track) {
        val trackList = getHistory().toMutableList()

        trackList.removeAll { it.trackName == track.trackName }

        trackList.add(0, track)

        if (trackList.size > 10) {
            trackList.removeAt(trackList.size - 1)
        }

        sharedPreferences.edit()
            .putString("history", gson.toJson(trackList))
            .apply()
    }

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString("history", "[]")
        return gson.fromJson(json, Array<Track>::class.java).toList()
    }

    fun clearHistory() {
        sharedPreferences.edit().remove("history").apply()
    }
}
