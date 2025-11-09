package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.domain.Resource
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {
    override fun searchTracks(expression: String): Flow<Resource<List<Track>>> = flow {
        val response = networkClient.doRequest(TrackSearchRequest(expression))

        when (response.resultCode) {
            200 -> {
                val tracks = (response as TrackSearchResponse).results.map { dto ->
                    TrackMapper.mapToDomain(dto)
                }
                emit(Resource.Success(tracks))
            }
            else -> emit(Resource.Error("Error"))
        }
    }
}