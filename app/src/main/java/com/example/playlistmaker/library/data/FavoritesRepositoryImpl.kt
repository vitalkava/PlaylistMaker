package com.example.playlistmaker.library.data

import com.example.playlistmaker.library.data.converters.TrackDbConverter
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.domain.FavoritesRepository
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val db: AppDatabase,
    private val convertor: TrackDbConverter
): FavoritesRepository {
    override suspend fun addToFavorites(track: Track) {
        val entity = convertor.map(track)
        withContext(Dispatchers.IO) {
            db.trackDao().insertTrackEntity(entity)
        }
    }

    override suspend fun removeFromFavorites(track: Track) {
        val entity = convertor.map(track)
        withContext(Dispatchers.IO) {
            db.trackDao().deleteTrackEntity(entity)
        }
    }

    override fun getFavorites(): Flow<List<Track>> {
        return db.trackDao().getTracks().map { entities ->
            entities.map { entity -> convertor.map(entity) } }
    }

    override suspend fun getFavoritesIds(): List<String> {
        return withContext(Dispatchers.IO) {
            db.trackDao().getId()
        }
    }
}