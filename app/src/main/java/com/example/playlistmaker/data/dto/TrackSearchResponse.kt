package com.example.playlistmaker.data.dto

data class TrackSearchResponse (
    val searchType: String,
    val expression: String,
    val results: List<TrackDto>
) : Response()