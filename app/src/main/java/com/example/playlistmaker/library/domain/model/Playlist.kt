package com.example.playlistmaker.library.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val coverUri: String? = null,
    val trackIds: List<String> = emptyList(),
    val trackCount: Int = 0,

    )