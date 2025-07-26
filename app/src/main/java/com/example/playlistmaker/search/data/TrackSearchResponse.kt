package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.data.Response

data class TrackSearchResponse (
    val searchType: String,
    val expression: String,
    val results: List<TrackDto>
) : Response()