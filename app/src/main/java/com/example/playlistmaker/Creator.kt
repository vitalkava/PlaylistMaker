package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.network.TracksRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.domain.api.ThemeRepository
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.impl.TracksInteractorImpl
import com.example.playlistmaker.domain.use_case.FilterTracksUseCase
import com.example.playlistmaker.domain.use_case.GetCurrentThemeUseCase
import com.example.playlistmaker.domain.use_case.SwitchThemeUseCase
import com.example.playlistmaker.domain.use_case.impl.FilterTracksUseCaseImpl

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

    fun provideFilterTracksUseCase(): FilterTracksUseCase {
        return FilterTracksUseCaseImpl()
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