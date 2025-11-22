package com.example.playlistmaker.library.data

import com.example.playlistmaker.library.data.converters.TrackDbConvertor
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.domain.FavoritesRepository
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val db: AppDatabase,
    private val convertor: TrackDbConvertor
): FavoritesRepository {
    override suspend fun addToFavorites(track: Track) {
        val entity = convertor.map(track)
        db.trackDao().insertTrackEntity(entity)
    }

    override suspend fun removeFromFavorites(track: Track) {
        val entity = convertor.map(track)
        db.trackDao().deleteTrackEntity(entity)
    }

    override fun getFavorites(): Flow<List<Track>> {
        return db.trackDao().getTracks().map { entities ->
            entities.map { entity -> convertor.map(entity) } }
    }
}