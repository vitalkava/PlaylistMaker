package com.example.playlistmaker.library.data

import com.example.playlistmaker.library.data.converters.TrackDbConverter
import com.example.playlistmaker.library.data.db.dao.PlaylistTrackDao
import com.example.playlistmaker.library.domain.PlaylistTrackRepository
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistTrackRepositoryImpl(
    private val playlistTrackDao: PlaylistTrackDao,
    private val converter: TrackDbConverter
) : PlaylistTrackRepository {

    override suspend fun insertTrack(track: Track) {
        val entity = converter.mapToPlaylistTrackEntity(track)
        withContext(Dispatchers.IO) {
            playlistTrackDao.insertTrack(entity)
        }
    }

    override fun getTracksByIds(trackIds: List<String>): Flow<List<Track>> {
        return playlistTrackDao.getTracksByIds(trackIds).map { entities ->
            entities.map { converter.mapFromPlaylistTrackEntity(it) }
        }
    }

    override suspend fun deleteTrack(trackId: String) {
        withContext(Dispatchers.IO) {
            playlistTrackDao.deleteTrack(trackId)
        }
    }
}