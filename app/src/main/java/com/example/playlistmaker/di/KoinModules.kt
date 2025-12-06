package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.playlistmaker.library.data.FavoritesInteractorImpl
import com.example.playlistmaker.library.data.FavoritesRepositoryImpl
import com.example.playlistmaker.library.data.PlaylistRepositoryImpl
import com.example.playlistmaker.library.data.PlaylistTrackRepositoryImpl
import com.example.playlistmaker.library.data.converters.PlaylistDbConverter
import com.example.playlistmaker.library.data.converters.TrackDbConverter
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.domain.FavoritesInteractor
import com.example.playlistmaker.library.domain.FavoritesRepository
import com.example.playlistmaker.library.domain.PlaylistInteractor
import com.example.playlistmaker.library.domain.PlaylistInteractorImpl
import com.example.playlistmaker.library.domain.PlaylistTrackRepository
import com.example.playlistmaker.library.domain.PlaylistsRepository
import com.example.playlistmaker.library.ui.favorites.FavoritesViewModel
import com.example.playlistmaker.library.ui.LibraryViewModel
import com.example.playlistmaker.library.ui.playlists.NewPlaylistViewModel
import com.example.playlistmaker.library.ui.playlists.PlaylistsViewModel
import com.example.playlistmaker.player.data.MediaPlayerFactory
import com.example.playlistmaker.player.data.MediaPlayerFactoryImpl
import com.example.playlistmaker.player.data.MediaPlayerRepositoryImpl
import com.example.playlistmaker.player.domain.AudioPlayerInteractor
import com.example.playlistmaker.player.domain.AudioPlayerInteractorImpl
import com.example.playlistmaker.player.domain.AudioPlayerRepository
import com.example.playlistmaker.player.ui.AudioPlayerViewModel
import com.example.playlistmaker.search.data.NetworkClient
import com.example.playlistmaker.search.data.RetrofitNetworkClient
import com.example.playlistmaker.search.data.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.TracksRepositoryImpl
import com.example.playlistmaker.search.data.ITunesApi
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.domain.TracksInteractorImpl
import com.example.playlistmaker.search.domain.TracksRepository
import com.example.playlistmaker.search.ui.SearchAdapter
import com.example.playlistmaker.search.ui.SearchViewModel
import com.example.playlistmaker.settings.data.ThemeRepositoryImpl
import com.example.playlistmaker.settings.domain.GetCurrentThemeUseCase
import com.example.playlistmaker.settings.domain.SwitchThemeUseCase
import com.example.playlistmaker.settings.domain.ThemeRepository
import com.example.playlistmaker.settings.ui.ExecutorFactory
import com.example.playlistmaker.settings.ui.ExecutorFactoryImpl
import com.example.playlistmaker.settings.ui.HandlerFactory
import com.example.playlistmaker.settings.ui.HandlerFactoryImpl
import com.example.playlistmaker.settings.ui.SettingsViewModel
import com.google.gson.Gson
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
        get<Retrofit>().create(ITunesApi::class.java)
    }

    single<NetworkClient> { RetrofitNetworkClient(get()) }

    single<ThemeRepository> { ThemeRepositoryImpl(get(SETTINGS_PREFS)) }

    single<AudioPlayerRepository> { MediaPlayerRepositoryImpl(get()) }

    single<MediaPlayerFactory> { MediaPlayerFactoryImpl() }

    single<ExecutorFactory> { ExecutorFactoryImpl() }

    single<HandlerFactory> { HandlerFactoryImpl() }

    single { Gson() }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "database.db"
        ).build()
    }

    single { PlaylistDbConverter(get<Gson>()) }
}

val domainModule = module {
    factory<GetCurrentThemeUseCase> { GetCurrentThemeUseCase(get()) }
    factory<SwitchThemeUseCase> { SwitchThemeUseCase(get()) }
    factory<TracksInteractor> { TracksInteractorImpl(get()) }
    factory<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }
    factory<AudioPlayerInteractor> { AudioPlayerInteractorImpl(get()) }
    factory<FavoritesInteractor> { FavoritesInteractorImpl(get()) }
    factory<PlaylistInteractor> {
        PlaylistInteractorImpl(
            get<PlaylistsRepository>(),
            get<PlaylistTrackRepository>()
        ) }
}

val viewModelModule = module {
    viewModel {
        SettingsViewModel(
            getCurrentThemeUseCase = get(),
            switchThemeUseCase = get()
        )
    }
    viewModel {
        SearchViewModel(
            tracksInteractor = get(),
            searchHistoryInteractor = get(),
        )
    }
    viewModel {
        AudioPlayerViewModel(
            audioInteractor = get(),
            favoritesInteractor = get(),
            playlistInteractor = get(),
        )
    }
    factory { (onTrackClicked: (Track) -> Unit) -> SearchAdapter(onTrackClicked) }

    viewModel { NewPlaylistViewModel(get(), get<PlaylistInteractor>()) }
}

val libraryModule = module {
    viewModel { LibraryViewModel() }
    viewModel { PlaylistsViewModel(get<PlaylistInteractor>()) }
    viewModel { FavoritesViewModel(get<FavoritesInteractor>()) }
}

val repositoryModule = module {
    factory { TrackDbConverter() }

    single <FavoritesRepository> {
        FavoritesRepositoryImpl(
            get<AppDatabase>(),
            get<TrackDbConverter>()
        )
    }

    single <TracksRepository>{
        TracksRepositoryImpl(
            get <NetworkClient>(),
            get <AppDatabase>()
        )
    }

    single <SearchHistoryRepository>{
        SearchHistoryRepositoryImpl(
            get(PLAYLIST_PREFS),
            get<AppDatabase>()
        )
    }

    single<PlaylistsRepository> {
        PlaylistRepositoryImpl(
            get<AppDatabase>(),
            get<PlaylistDbConverter>()
        )
    }

    single<PlaylistTrackRepository> {
        PlaylistTrackRepositoryImpl(
            get<AppDatabase>(),
            get<TrackDbConverter>()
        )
    }
}
