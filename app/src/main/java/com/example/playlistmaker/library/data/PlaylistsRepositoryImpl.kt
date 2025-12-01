package com.example.playlistmaker.library.data

import com.example.playlistmaker.library.data.converters.PlaylistDbConverter
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.domain.PlaylistsRepository
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistRepositoryImpl(
    private val db: AppDatabase,
    private val converter: PlaylistDbConverter
) : PlaylistsRepository {

    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = converter.map(playlist)
        return withContext(Dispatchers.IO) {
            db.playlistDao().insertPlaylist(entity)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = converter.map(playlist)
        withContext(Dispatchers.IO) {
            db.playlistDao().updatePlaylist(entity)
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return db.playlistDao().getPlaylists().map { entities ->
            entities.map { converter.map(it) }
        }
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        return withContext(Dispatchers.IO) {
            db.playlistDao().getPlaylistById(id)?.let { converter.map(it) }
        }
    }

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        val updatedTrackIds = playlist.trackIds.toMutableList().apply { add(track.trackId) }
        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size
        )
        updatePlaylist(updatedPlaylist)
    }

    override suspend fun deletePlaylist(id: Long) {
        withContext(Dispatchers.IO) {
            db.playlistDao().deletePlaylist(id)
        }
    }
}