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

open class NewPlaylistViewModel(
    application: Application,
    private val interactor: PlaylistInteractor
) : AndroidViewModel(application) {

    protected val _state = MutableLiveData(NewPlaylistState())
    open val state: LiveData<NewPlaylistState> = _state
    protected val _event = MutableLiveData<NewPlaylistEvent>()
    val event: LiveData<NewPlaylistEvent> = _event

    open fun updateName(name: String) {
        val trimmed = name.trim()
        _state.value = _state.value?.copy(
            name = trimmed,
            isCreateEnabled = trimmed.isNotBlank(),
            hasChanges = trimmed.isNotBlank() ||
                    _state.value?.description?.isNotBlank() == true ||
                    _state.value?.coverUri != null
        )
    }

    open fun updateDescription(description: String) {
        val trimmed = description.trim()
        _state.value = _state.value?.copy(
            description = trimmed,
            hasChanges = _state.value?.name?.isNotBlank() == true ||
                    trimmed.isNotBlank() ||
                    _state.value?.coverUri != null
        )
    }

    open fun pickCover(uri: Uri) {
        val savedUri = saveImageToInternalStorage(uri)
        _state.value = _state.value?.copy(
            coverUri = savedUri,
            hasChanges = true
        )
    }

    open fun createPlaylist() {
        val current = _state.value ?: return
        if (!current.isCreateEnabled) return

        val playlist = Playlist(
            name = current.name,
            description = current.description.takeIf { it.isNotBlank() },
            coverUri = current.coverUri?.toString()
        )

        viewModelScope.launch {
            val newId = interactor.createPlaylist(playlist)
            _event.postValue(NewPlaylistEvent.Created(current.name))
        }
    }

    open fun handleBack() {
        if (_state.value?.hasChanges == true) {
            _event.postValue(NewPlaylistEvent.ShowConfirmDialog)
        } else {
            _event.postValue(NewPlaylistEvent.NavigateBack)
        }
    }

    protected fun saveImageToInternalStorage(uri: Uri): Uri {
        val file = File(
            getApplication<Application>().filesDir,
            "playlist_cover_${UUID.randomUUID()}.jpg"
        )
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(file)
    }
}