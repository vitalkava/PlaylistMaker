package com.example.playlistmaker.library.ui.playlists

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.PlaylistInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    application: Application,
    private val interactor: PlaylistInteractor
) : NewPlaylistViewModel(application, interactor) {

    private var originalPlaylist: Playlist? = null

    fun loadPlaylistForEdit(playlist: Playlist) {
        originalPlaylist = playlist
        _state.value = NewPlaylistState(
            name = playlist.name,
            description = playlist.description.orEmpty(),
            coverUri = playlist.coverUri?.let { Uri.parse(it) },
            isCreateEnabled = playlist.name.isNotBlank(),
            hasChanges = false
        )
    }

    override fun updateName(name: String) {
        val trimmed = name.trim()
        val hasChanges = trimmed != originalPlaylist?.name ||
                _state.value?.description?.trim() != originalPlaylist?.description ||
                _state.value?.coverUri?.toString() != originalPlaylist?.coverUri

        _state.value = _state.value?.copy(
            name = trimmed,
            isCreateEnabled = trimmed.isNotBlank(),
            hasChanges = hasChanges
        )
    }

    override fun updateDescription(description: String) {
        val trimmed = description.trim()
        val hasChanges = trimmed != originalPlaylist?.description ||
                _state.value?.name?.trim() != originalPlaylist?.name ||
                _state.value?.coverUri?.toString() != originalPlaylist?.coverUri

        _state.value = _state.value?.copy(
            description = trimmed,
            hasChanges = hasChanges
        )
    }

    override fun pickCover(uri: Uri) {
        super.pickCover(uri)
        _state.value = _state.value?.copy(hasChanges = true)
    }

    fun saveEditedPlaylist() {
        val state = _state.value ?: return
        val playlist = originalPlaylist ?: return
        if (state.name.isBlank()) return

        val updated = playlist.copy(
            name = state.name,
            description = state.description.takeIf { it.isNotBlank() },
            coverUri = state.coverUri?.toString()
        )

        viewModelScope.launch {
            interactor.updatePlaylist(updated)
            _event.postValue(NewPlaylistEvent.Created(updated.name))
        }
    }

    override fun handleBack() {
        _event.postValue(NewPlaylistEvent.NavigateBack)
    }
}