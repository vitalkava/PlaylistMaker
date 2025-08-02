package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import com.example.playlistmaker.player.data.MediaPlayerRepositoryImpl
import com.example.playlistmaker.player.domain.AudioPlayerInteractor
import com.example.playlistmaker.player.domain.AudioPlayerInteractorImpl
import com.example.playlistmaker.player.domain.AudioPlayerRepository
import com.example.playlistmaker.player.ui.AudioPlayerViewModel
import com.example.playlistmaker.search.data.NetworkClient
import com.example.playlistmaker.search.data.RetrofitNetworkClient
import com.example.playlistmaker.search.data.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.TracksRepositoryImpl
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.domain.TracksInteractorImpl
import com.example.playlistmaker.search.domain.TracksRepository
import com.example.playlistmaker.search.ui.SearchViewModel
import com.example.playlistmaker.settings.data.ThemeRepositoryImpl
import com.example.playlistmaker.settings.domain.GetCurrentThemeUseCase
import com.example.playlistmaker.settings.domain.SwitchThemeUseCase
import com.example.playlistmaker.settings.domain.ThemeRepository
import com.example.playlistmaker.settings.ui.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val SETTINGS_PREFS = named("SETTINGS_PREFS")
val PLAYLIST_PREFS = named("PLAYLIST_PREFS")


val dataModule = module {
    single<SharedPreferences>(SETTINGS_PREFS) {
        androidContext().getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
    }

    single<SharedPreferences>(PLAYLIST_PREFS) {
        androidContext().getSharedPreferences("playlist_prefs", Context.MODE_PRIVATE)
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(com.example.playlistmaker.search.data.iTunesApi::class.java)
    }

    single<NetworkClient> { RetrofitNetworkClient }

    single<ThemeRepository> { ThemeRepositoryImpl(get(SETTINGS_PREFS)) }

    single<com.example.playlistmaker.search.domain.SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(PLAYLIST_PREFS))
    }

    single<TracksRepository> { TracksRepositoryImpl(get()) }

    single<AudioPlayerRepository> { MediaPlayerRepositoryImpl(MediaPlayer()) }
}

val domainModule = module {
    factory<GetCurrentThemeUseCase> { GetCurrentThemeUseCase(get()) }
    factory<SwitchThemeUseCase> { SwitchThemeUseCase(get()) }
    factory<TracksInteractor> { TracksInteractorImpl(get()) }
    factory<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }
    factory<AudioPlayerInteractor> { AudioPlayerInteractorImpl(get()) }
}

val viewModelModule = module {
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { AudioPlayerViewModel(get()) }
}