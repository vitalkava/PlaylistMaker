package com.example.playlistmaker.search.domain

class SearchHistoryInteractorImpl(private val repository: SearchHistoryRepository): SearchHistoryInteractor {

    override suspend fun saveTrack(track: Track) {
        repository.saveTrack(track = track)
    }

    override suspend fun getHistory(): List<Track> {
        return repository.getHistory()
    }

    override fun clearHistory() {
        repository.clearHistory()
    }
}