package com.example.playlistmaker.library.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.FavoritesInteractor
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.launch

sealed class FavoritesState {
    object Empty : FavoritesState()
    data class Content(val tracks: List<Track>) : FavoritesState()
}

class FavoritesViewModel(
    private val interactor: FavoritesInteractor
) : ViewModel() {
    private val _state = MutableLiveData<FavoritesState>()
    val state: LiveData<FavoritesState> = _state

    init {
        viewModelScope.launch {
            interactor.getFavorites().collect { tracks ->
                if (tracks.isEmpty()) {
                    _state.postValue(FavoritesState.Empty)
                } else {
                    _state.postValue(FavoritesState.Content(tracks))
                }
            }
        }
    }
}