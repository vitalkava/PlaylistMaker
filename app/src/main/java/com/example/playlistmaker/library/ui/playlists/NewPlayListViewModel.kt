package com.example.playlistmaker.library.ui.playlists

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.PlaylistInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed class NewPlaylistEvent {
    data class Created(val name: String) : NewPlaylistEvent()
    object ShowConfirmDialog : NewPlaylistEvent()
    object NavigateBack : NewPlaylistEvent()
}
class NewPlaylistViewModel(
    application: Application,
    private val interactor: PlaylistInteractor
) : AndroidViewModel(application) {

    private val _state = MutableLiveData(NewPlaylistState())
    val state: LiveData<NewPlaylistState> = _state

    private val _event = MutableLiveData<NewPlaylistEvent>()
    val event: LiveData<NewPlaylistEvent> = _event

    fun updateName(name: String) {
        val newState = _state.value?.copy(
            name = name.trim(),
            isCreateEnabled = name.trim().isNotBlank(),
            hasChanges = name.trim().isNotBlank() || _state.value?.description?.isNotBlank() == true || _state.value?.coverUri != null
        )
        _state.value = newState
    }

    fun updateDescription(description: String) {
        val newState = _state.value?.copy(
            description = description.trim(),
            hasChanges = _state.value?.name?.isNotBlank() == true || description.trim().isNotBlank() || _state.value?.coverUri != null
        )
        _state.value = newState
    }

    fun pickCover(uri: Uri) {
        val savedUri = saveImageToInternalStorage(uri)
        val newState = _state.value?.copy(
            coverUri = savedUri,
            hasChanges = true
        )
        _state.value = newState
    }

    fun createPlaylist() {
        val current = _state.value ?: return
        if (current.isCreateEnabled) {
            val playlist = Playlist(
                id = 0,
                name = current.name,
                description = current.description.takeIf { it.isNotBlank() },
                coverUri = current.coverUri?.toString()
            )
            viewModelScope.launch {
                val newId = interactor.createPlaylist(playlist)
                val updated = playlist.copy(id = newId)
                _event.postValue(NewPlaylistEvent.Created(current.name))
            }
        }
    }

    fun handleBack() {
        if (_state.value?.hasChanges == true) {
            _event.postValue(NewPlaylistEvent.ShowConfirmDialog)
        } else {
            _event.postValue(NewPlaylistEvent.NavigateBack)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri {
        val file =
            File(getApplication<Application>().filesDir, "playlist_cover_${UUID.randomUUID()}.jpg")
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(file)
    }
}