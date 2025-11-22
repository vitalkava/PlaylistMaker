package com.example.playlistmaker.library.data

import com.example.playlistmaker.library.domain.FavoritesInteractor
import com.example.playlistmaker.library.domain.FavoritesRepository
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

class FavoritesInteractorImpl(private val repository: FavoritesRepository): FavoritesInteractor {
    override suspend fun addToFavorites(track: Track) = repository.addToFavorites(track)
    override suspend fun removeFromFavorites(track: Track) = repository.removeFromFavorites(track)
    override fun getFavorites(): Flow<List<Track>> = repository.getFavorites()
}