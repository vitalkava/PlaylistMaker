package com.example.playlistmaker.settings.ui

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface ExecutorFactory {
    fun createSingleThreadExecutor(): ExecutorService
}

class ExecutorFactoryImpl : ExecutorFactory {
    override fun createSingleThreadExecutor(): ExecutorService = Executors.newSingleThreadExecutor()
}