package com.example.playlistmaker.settings.ui

import android.os.Handler
import android.os.Looper

interface HandlerFactory {
    fun createMainHandler(): Handler
}

class HandlerFactoryImpl : HandlerFactory {
    override fun createMainHandler(): Handler = Handler(Looper.getMainLooper())
}