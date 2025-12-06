package com.example.playlistmaker.library.domain

import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val playlistRepository: PlaylistsRepository,
    private val trackRepository: PlaylistTrackRepository
) : PlaylistInteractor {

    override suspend fun createPlaylist(playlist: Playlist): Long = playlistRepository.createPlaylist(playlist)

    override suspend fun updatePlaylist(playlist: Playlist) = playlistRepository.updatePlaylist(playlist)

    override fun getPlaylists(): Flow<List<Playlist>> = playlistRepository.getPlaylists()

    override suspend fun getPlaylistById(id: Long): Playlist? = playlistRepository.getPlaylistById(id)

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        playlistRepository.addTrackToPlaylist(playlist, track)
        insertTrackToStorage(track)
    }

    override suspend fun deletePlaylist(id: Long) = playlistRepository.deletePlaylist(id)

    override suspend fun insertTrackToStorage(track: Track) = trackRepository.insertTrack(track)

    override fun getTracksByIds(trackIds: List<String>): Flow<List<Track>> = trackRepository.getTracksByIds(trackIds)
}