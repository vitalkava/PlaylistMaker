package com.example.playlistmaker.search.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TracksInteractor
import java.util.concurrent.Executors

sealed class SearchScreenState {
    data object Loading : SearchScreenState()
    data class SearchResults(val tracks: List<Track>) : SearchScreenState()
    data class History(val tracks: List<Track>) : SearchScreenState()
    data object NoResults : SearchScreenState()
    data object NoInternet : SearchScreenState()
    data object Empty : SearchScreenState()
}

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private val _screenState = MutableLiveData<SearchScreenState>(SearchScreenState.Empty)
    val screenState: LiveData<SearchScreenState> = _screenState

    private var currentQuery = ""
    private var lastQuery = ""
    private var lastSearchResults: List<Track>? = null

    fun onQueryChanged(text: String) {
        currentQuery = text.trim()
        handler.removeCallbacks(searchDebounceRunnable)
        if (currentQuery.isEmpty()) {
            lastQuery = ""
            lastSearchResults = null
            loadHistory()
        } else {
            if (currentQuery == lastQuery && lastSearchResults != null) {
                _screenState.postValue(SearchScreenState.SearchResults(lastSearchResults!!))
            } else {
                handler.postDelayed(searchDebounceRunnable, 1000)
            }
        }
    }

    private val searchDebounceRunnable = Runnable {
        performSearchInternal(currentQuery)
    }

    private fun performSearchInternal(query: String) {
        _screenState.postValue(SearchScreenState.Loading)
        lastQuery = query
        lastSearchResults = null
        executor.execute {
            tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
                override fun consume(found: List<Track>) {
                    lastSearchResults = found
                    if (found.isEmpty()) {
                        _screenState.postValue(SearchScreenState.NoResults)
                    } else {
                        _screenState.postValue(SearchScreenState.SearchResults(found))
                    }
                }

                override fun onError(error: Throwable) {
                    lastSearchResults = null
                    _screenState.postValue(SearchScreenState.NoInternet)
                }
            })
        }
    }

    fun saveTrackToHistory(track: Track) {
        executor.execute {
            searchHistoryInteractor.saveTrack(track)
        }
    }

    fun loadHistory() {
        executor.execute {
            val history = searchHistoryInteractor.getHistory()
            if (history.isNotEmpty() && currentQuery.isEmpty()) {
                _screenState.postValue(SearchScreenState.History(history))
            } else if (currentQuery.isNotEmpty() && lastSearchResults != null) {
                _screenState.postValue(SearchScreenState.SearchResults(lastSearchResults!!))
            } else {
                _screenState.postValue(SearchScreenState.Empty)
            }
        }
    }

    fun clearHistory() {
        executor.execute {
            searchHistoryInteractor.clearHistory()
            if (currentQuery.isEmpty()) {
                _screenState.postValue(SearchScreenState.Empty)
            }
        }
    }

    fun retrySearch() {
        if (currentQuery.isNotBlank()) {
            performSearchInternal(currentQuery)
        }
    }

    override fun onCleared() {
        super.onCleared()
        executor.shutdown()
        handler.removeCallbacksAndMessages(null)
    }
}