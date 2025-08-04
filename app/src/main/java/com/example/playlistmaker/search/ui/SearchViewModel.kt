package com.example.playlistmaker.search.ui

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.settings.ui.ExecutorFactory
import com.example.playlistmaker.settings.ui.HandlerFactory
import java.util.concurrent.ExecutorService

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
    private val searchHistoryInteractor: SearchHistoryInteractor,
    private val executorFactory: ExecutorFactory,
    private val handlerFactory: HandlerFactory
) : ViewModel() {
    private val executor: ExecutorService = executorFactory.createSingleThreadExecutor()
    private val handler: Handler = handlerFactory.createMainHandler()
    private val _screenState = MutableLiveData(SearchScreenState())
    val screenState: LiveData<SearchScreenState> = _screenState

    private val searchDebounceRunnable = Runnable { performSearchInternal() }

    init {
        loadHistory()
    }

    fun onQueryChanged(text: String) {
        val newQuery = text.trim()
        if (newQuery == _screenState.value?.query) {
            return
        }

        _screenState.postValue(_screenState.value?.copy(query = newQuery))

        handler.removeCallbacks(searchDebounceRunnable)
        if (newQuery.isEmpty()) {
            loadHistory()
        } else {
            handler.postDelayed(searchDebounceRunnable, 1000)
        }
    }

    private fun performSearchInternal() {
        val query = _screenState.value?.query ?: return
        _screenState.postValue(_screenState.value?.copy(isLoading = true))

        executor.execute {
            tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
                override fun consume(found: List<Track>) {
                    _screenState.postValue(
                        _screenState.value?.copy(
                            isLoading = false,
                            tracks = found,
                            isHistoryVisible = false,
                            isNoResultsVisible = found.isEmpty(),
                            isNoInternetVisible = false
                        )
                    )
                }

                override fun onError(error: Throwable) {
                    _screenState.postValue(
                        _screenState.value?.copy(
                            isLoading = false,
                            tracks = emptyList(),
                            isHistoryVisible = false,
                            isNoResultsVisible = false,
                            isNoInternetVisible = true
                        )
                    )
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
            _screenState.postValue(
                _screenState.value?.copy(
                    isLoading = false,
                    tracks = if (_screenState.value?.query.isNullOrEmpty()) history else emptyList(),
                    isHistoryVisible = history.isNotEmpty() && _screenState.value?.query.isNullOrEmpty(),
                    isNoResultsVisible = false,
                    isNoInternetVisible = false
                )
            )
        }
    }

    fun clearHistory() {
        executor.execute {
            searchHistoryInteractor.clearHistory()
            if (_screenState.value?.query.isNullOrEmpty()) {
                _screenState.postValue(
                    _screenState.value?.copy(
                        tracks = emptyList(),
                        isHistoryVisible = false
                    )
                )
            }
        }
    }

    fun retrySearch() {
        if (_screenState.value?.query?.isNotBlank() == true) {
            performSearchInternal()
        }
    }

    override fun onCleared() {
        super.onCleared()
        executor.shutdown()
        handler.removeCallbacksAndMessages(null)
    }
}