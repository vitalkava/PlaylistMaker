package com.example.playlistmaker.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.di.dataModule
import com.example.playlistmaker.di.domainModule
import com.example.playlistmaker.di.libraryModule
import com.example.playlistmaker.di.viewModelModule
import com.example.playlistmaker.settings.domain.GetCurrentThemeUseCase
import com.example.playlistmaker.settings.domain.SwitchThemeUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.android.ext.android.inject

class App : Application() {

    private val switchThemeUseCase: SwitchThemeUseCase by inject()
    private val getCurrentThemeUseCase: GetCurrentThemeUseCase by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(dataModule, domainModule, viewModelModule, libraryModule)
        }

        val isDarkTheme = getCurrentThemeUseCase.execute()
        switchTheme(isDarkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        switchThemeUseCase.execute(darkThemeEnabled)
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}