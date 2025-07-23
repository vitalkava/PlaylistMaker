package com.example.playlistmaker.search.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TracksInteractor
import java.util.concurrent.Executors

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor,
) : ViewModel() {

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private val _searchResults = MutableLiveData<List<Track>>(emptyList())
    val searchResults: LiveData<List<Track>> = _searchResults

    private val _history = MutableLiveData<List<Track>>(emptyList())
    val history: LiveData<List<Track>> = _history

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val searchDebounceRunnable = Runnable {
        performSearchInternal(currentQuery)
    }
    private var currentQuery = ""

    fun onQueryChanged(text: String) {
        currentQuery = text.trim()
        handler.removeCallbacks(searchDebounceRunnable)
        if (currentQuery.isEmpty()) {
            _searchResults.value = emptyList()
            loadHistory()
        } else {
            handler.postDelayed(searchDebounceRunnable, 1000)
        }
    }

    private fun performSearchInternal(query: String) {
        _isLoading.postValue(true)
        executor.execute {
            tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
                override fun consume(found: List<Track>) {
                    _searchResults.postValue(found)
                    _isLoading.postValue(false)
                    _errorMessage.postValue(if (found.isEmpty()) "nothing_found" else null)
                }

                override fun onError(error: Throwable) {
                    _searchResults.postValue(emptyList())
                    _isLoading.postValue(false)
                    _errorMessage.postValue("no_internet")
                }
            })

        }
    }

    fun saveTrackToHistory(track: Track) {
        executor.execute {
            searchHistoryInteractor.saveTrack(track)
            loadHistory()
        }
    }

    fun loadHistory() {
        executor.execute {
            val h = searchHistoryInteractor.getHistory()
            _history.postValue(h)
        }
    }

    fun clearHistory() {
        executor.execute {
            searchHistoryInteractor.clearHistory()
            _history.postValue(emptyList())
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
