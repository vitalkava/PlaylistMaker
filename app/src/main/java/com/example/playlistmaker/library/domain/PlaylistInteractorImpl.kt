package com.example.playlistmaker.library.domain

import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PlaylistInteractorImpl(
    private val playlistRepository: PlaylistsRepository,
    private val trackRepository: PlaylistTrackRepository
) : PlaylistInteractor {
    override suspend fun createPlaylist(playlist: Playlist): Long =
        playlistRepository.createPlaylist(playlist)

    override suspend fun updatePlaylist(playlist: Playlist) =
        playlistRepository.updatePlaylist(playlist)

    override fun getPlaylists(): Flow<List<Playlist>> =
        playlistRepository.getPlaylists()

    override suspend fun getPlaylistById(id: Long): Playlist? =
        playlistRepository.getPlaylistById(id)

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        playlistRepository.addTrackToPlaylist(playlist, track)
        insertTrackToStorage(track)
    }

    override suspend fun deletePlaylist(id: Long) {
        val playlist = playlistRepository.getPlaylistById(id) ?: return
        playlistRepository.deletePlaylist(id)
        val allPlaylists = playlistRepository.getPlaylists().first()
        playlist.trackIds.forEach { trackId ->
            val isUsed = allPlaylists.any { trackId in it.trackIds }
            if (!isUsed) {
                trackRepository.deleteTrack(trackId)
            }
        }
    }

    override suspend fun insertTrackToStorage(track: Track) =
        trackRepository.insertTrack(track)

    override fun getTracksByIds(trackIds: List<String>): Flow<List<Track>> =
        trackRepository.getTracksByIds(trackIds)

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        val playlist = playlistRepository.getPlaylistById(playlistId) ?: return

        val updatedIds = playlist.trackIds.toMutableList().apply { remove(trackId) }
        val updatedPlaylist = playlist.copy(
            trackIds = updatedIds,
            trackCount = updatedIds.size
        )

        playlistRepository.updatePlaylist(updatedPlaylist)

        val allPlaylists = playlistRepository.getPlaylists().first()
        val isUsed = allPlaylists.any { trackId in it.trackIds }
        if (!isUsed) {
            trackRepository.deleteTrack(trackId)
        }
    }
}