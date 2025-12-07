package com.example.playlistmaker.library.data

import com.example.playlistmaker.library.data.converters.TrackDbConverter
import com.example.playlistmaker.library.data.db.dao.TrackDao
import com.example.playlistmaker.library.domain.FavoritesRepository
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val trackDao: TrackDao,
    private val convertor: TrackDbConverter
): FavoritesRepository {
    override suspend fun addToFavorites(track: Track) {
        val entity = convertor.map(track)
        withContext(Dispatchers.IO) {
            trackDao.insertTrackEntity(entity)
        }
    }

    override suspend fun removeFromFavorites(track: Track) {
        val entity = convertor.map(track)
        withContext(Dispatchers.IO) {
            trackDao.deleteTrackEntity(entity)
        }
    }

    override fun getFavorites(): Flow<List<Track>> {
        return trackDao.getTracks().map { entities ->
            entities.map { entity -> convertor.map(entity) } }
    }

    override suspend fun getFavoritesIds(): List<String> {
        return withContext(Dispatchers.IO) {
            trackDao.getId()
        }
    }
}