package com.example.playlistmaker.domain.use_case

import com.example.playlistmaker.search.domain.Track

interface FilterTracksUseCase {
    fun execute(tracks: List<Track>, query: String): List<Track>
}