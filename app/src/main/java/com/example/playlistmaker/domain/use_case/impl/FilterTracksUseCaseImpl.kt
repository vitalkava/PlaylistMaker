package com.example.playlistmaker.domain.use_case.impl

import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.domain.use_case.FilterTracksUseCase

class FilterTracksUseCaseImpl: FilterTracksUseCase {
    override fun execute(tracks: List<Track>, query: String): List<Track> {
        return tracks.filter {
            it.trackName.contains(query, ignoreCase = true) ||
                    it.artistName.contains(query, ignoreCase = true)
        }
    }
}