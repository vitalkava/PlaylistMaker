package com.example.playlistmaker.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.TracksInteractor

class SearchViewModelFactory(
    private val ti: TracksInteractor,
    private val shi: SearchHistoryInteractor,
) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(c: Class<T>): T {
        if (c.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(ti, shi) as T
        }
        throw IllegalArgumentException()
    }
}