package com.example.playlistmaker.search.domain

import java.io.IOException

class TracksInteractorImpl(private val repository: TracksRepository): TracksInteractor {

    override fun searchTracks(query: String, consumer: TracksInteractor.TracksConsumer) {
        try {
            val result = repository.searchTracks(query)
            consumer.consume(result)
        } catch (e: IOException) {
            consumer.onError(e)
        } catch (e: Exception) {
            consumer.onError(e)
        }
    }
}