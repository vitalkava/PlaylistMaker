package com.example.playlistmaker.creator

import android.content.Context
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.data.TracksRepositoryImpl
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.domain.TracksInteractorImpl
import com.example.playlistmaker.search.domain.TracksRepository
import com.example.playlistmaker.settings.data.ThemeRepositoryImpl
import com.example.playlistmaker.settings.domain.GetCurrentThemeUseCase
import com.example.playlistmaker.settings.domain.SwitchThemeUseCase
import com.example.playlistmaker.settings.domain.ThemeRepository

object Creator {
    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient)
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        val sharedPreferences = context.getSharedPreferences("playlist_prefs", Context.MODE_PRIVATE)
        val repository = SearchHistoryRepositoryImpl(sharedPreferences)
        return SearchHistoryInteractorImpl(repository)
    }

    fun provideThemeRepository(context: Context): ThemeRepository {
        val prefs = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        return ThemeRepositoryImpl(prefs)
    }

    fun provideSwitchThemeUseCase(context: Context): SwitchThemeUseCase {
        return SwitchThemeUseCase(provideThemeRepository(context))
    }

    fun provideGetCurrentThemeUseCase(context: Context): GetCurrentThemeUseCase {
        return GetCurrentThemeUseCase(provideThemeRepository(context))
    }

}