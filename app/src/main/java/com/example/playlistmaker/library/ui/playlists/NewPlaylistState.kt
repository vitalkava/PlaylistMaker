package com.example.playlistmaker.library.ui.playlists

import android.net.Uri

data class NewPlaylistState(
    val name: String = "",
    val description: String = "",
    val coverUri: Uri? = null,
    val isCreateEnabled: Boolean = false,
    val hasChanges: Boolean = false
)
