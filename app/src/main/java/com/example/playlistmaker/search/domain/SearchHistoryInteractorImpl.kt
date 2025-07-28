package com.example.playlistmaker.search.domain

class SearchHistoryInteractorImpl(private val repository: SearchHistoryRepository): SearchHistoryInteractor {

    override fun saveTrack(track: Track) {
        repository.saveTrack(track = track)
    }

    override fun getHistory(): List<Track> {
        return repository.getHistory()
    }

    override fun clearHistory() {
        repository.clearHistory()
    }
}