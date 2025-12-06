package com.example.playlistmaker.library.data.converters

import com.example.playlistmaker.library.data.db.entity.PlaylistEntity
import com.example.playlistmaker.library.domain.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class PlaylistDbConverter(private val gson: Gson) {

    fun map(entity: PlaylistEntity): Playlist {
        val trackIdsType = object : TypeToken<List<String>>() {}.type
        val trackIds: List<String> = gson.fromJson(entity.trackIdsJson, trackIdsType)
        return Playlist(
            id = entity.playlistId,
            name = entity.name,
            description = entity.description,
            coverUri = entity.coverUri,
            trackIds = trackIds,
            trackCount = entity.trackCount
        )
    }

    fun map(playlist: Playlist): PlaylistEntity {
        val trackIdsJson = gson.toJson(playlist.trackIds)
        return PlaylistEntity(
            playlistId = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverUri = playlist.coverUri,
            trackIdsJson = trackIdsJson,
            trackCount = playlist.trackCount
        )
    }
}