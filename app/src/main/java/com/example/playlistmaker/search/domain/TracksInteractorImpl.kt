package com.example.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TracksInteractorImpl(private val repository: TracksRepository): TracksInteractor {

    override fun searchTracks(query: String): Flow<Resource<List<Track>>> {
        return repository.searchTracks(query).map { it }
    }
}