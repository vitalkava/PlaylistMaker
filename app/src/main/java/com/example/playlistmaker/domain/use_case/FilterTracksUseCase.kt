package com.example.playlistmaker.domain.use_case

import com.example.playlistmaker.domain.models.Track

interface FilterTracksUseCase {
    fun execute(tracks: List<Track>, query: String): List<Track>
}