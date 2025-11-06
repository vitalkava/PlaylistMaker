package com.example.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.Resource
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TracksInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SearchScreenState(
    val query: String = "",
    val isLoading: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val isHistoryVisible: Boolean = false,
    val isNoResultsVisible: Boolean = false,
    val isNoInternetVisible: Boolean = false
)

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 1000L
    }

    private var searchJob: Job? = null
    private val _screenState = MutableLiveData(SearchScreenState())
    val screenState: LiveData<SearchScreenState> = _screenState

    init {
        loadHistory()
    }

    fun onQueryChanged(text: String) {
        val newQuery = text.trim()
        if (newQuery == _screenState.value?.query) return

        _screenState.value = _screenState.value?.copy(query = newQuery)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(newQuery)
        }

        if (newQuery.isEmpty()) {
            loadHistory()
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                tracksInteractor.searchTracks(query)
                    .collect { resource ->
                        processResult(resource)
                    }
            }
        }
    }

    private fun processResult(resource: Resource<List<Track>>) {
        when (resource) {
            is Resource.Success -> {
                val tracks = resource.data
                _screenState.value = _screenState.value?.copy(
                    isLoading = false,
                    tracks = tracks,
                    isHistoryVisible = false,
                    isNoResultsVisible = tracks.isEmpty(),
                    isNoInternetVisible = false,
                )
            }

            is Resource.Error -> {
                _screenState.value = _screenState.value?.copy(
                    isLoading = false,
                    tracks = emptyList(),
                    isHistoryVisible = false,
                    isNoResultsVisible = false,
                    isNoInternetVisible = true,
                )

            }
        }
    }

    fun saveTrackToHistory(track: Track) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                searchHistoryInteractor.saveTrack(track)
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) {
                searchHistoryInteractor.getHistory()
            }
            _screenState.value = _screenState.value?.copy(
                isLoading = false,
                tracks = if (_screenState.value?.query.isNullOrEmpty()) {
                    history
                } else {
                    _screenState.value?.tracks ?: emptyList()
                },
                isHistoryVisible = history.isNotEmpty() && _screenState.value?.query.isNullOrEmpty(),
                isNoResultsVisible = false,
                isNoInternetVisible = false
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                searchHistoryInteractor.clearHistory()
            }
            if (_screenState.value?.query.isNullOrEmpty()) {
                _screenState.value = _screenState.value?.copy(
                    tracks = emptyList(),
                    isHistoryVisible = false
                )
            }
        }
    }

    fun retrySearch() {
        val query = _screenState.value?.query
        if (!query.isNullOrBlank()) {
            performSearch(query)
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}