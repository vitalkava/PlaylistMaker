package com.example.playlistmaker.search.data

import com.example.playlistmaker.data.NetworkClient
import com.example.playlistmaker.search.domain.TracksRepository
import com.example.playlistmaker.search.domain.Track

class TracksRepositoryImpl(private val networkClient: NetworkClient): TracksRepository {
    override fun searchTracks(expression: String): List<Track> {
        val response = networkClient.doRequest(TrackSearchRequest(expression))
        if (response.resultCode == 200) {
            return (response as TrackSearchResponse).results.map {
                TrackMapper.mapToDomain(it)
            }
        } else {
            return emptyList()
        }
    }
}