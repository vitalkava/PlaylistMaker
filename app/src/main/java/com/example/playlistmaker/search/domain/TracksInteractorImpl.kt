package com.example.playlistmaker.search.domain

import java.io.IOException

class TracksInteractorImpl(private val repository: TracksRepository): TracksInteractor {

    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            repository.searchTracks(query)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }
}