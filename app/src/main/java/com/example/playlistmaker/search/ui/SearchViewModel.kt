package com.example.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private var searchDebounceJob: Job? = null
    private var saveThrottleJob: Job? = null
    private val _screenState = MutableLiveData(SearchScreenState())
    val screenState: LiveData<SearchScreenState> = _screenState

    init {
        loadHistory()
    }

    fun onQueryChanged(text: String) {
        val newQuery = text.trim()
        if (newQuery == _screenState.value?.query) {
            return
        }

        _screenState.postValue(_screenState.value?.copy(query = newQuery))
        searchDebounceJob?.cancel()

        if (newQuery.isEmpty()) {
            loadHistory()
        } else {
            searchDebounceJob = viewModelScope.launch {
                delay(1000)
                performSearchInternal()
            }
        }
    }

    private suspend fun performSearchInternal() {
        val query = _screenState.value?.query ?: return
        _screenState.value = _screenState.value?.copy(isLoading = true)

        try {
            val foundTracks = withContext(Dispatchers.IO) {
                tracksInteractor.searchTracks(query)
            }

            withContext(Dispatchers.Main) {
                _screenState.value = _screenState.value?.copy(
                    isLoading = false,
                    tracks = foundTracks,
                    isHistoryVisible = false,
                    isNoResultsVisible = foundTracks.isEmpty(),
                    isNoInternetVisible = false
                )
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _screenState.value = _screenState.value?.copy(
                    isLoading = false,
                    tracks = emptyList(),
                    isHistoryVisible = false,
                    isNoResultsVisible = false,
                    isNoInternetVisible = true
                )
            }
        }
    }

    fun saveTrackToHistory(track: Track) {
        saveThrottleJob?.cancel()
        saveThrottleJob = viewModelScope.launch {
            if (saveThrottleJob?.isActive == true) {
                delay(500)
            }

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
                tracks = if (_screenState.value?.query.isNullOrEmpty()) history else _screenState.value?.tracks
                    ?: emptyList(),
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
            searchDebounceJob?.cancel()
            searchDebounceJob = viewModelScope.launch {
                delay(0)
                performSearchInternal()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchDebounceJob?.cancel()
        saveThrottleJob?.cancel()
    }
}