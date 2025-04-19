package com.example.playlistmaker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val I_TUNES_BASE_URL = "https://itunes.apple.com"
    private val retrofit = Retrofit
        .Builder()
        .baseUrl(I_TUNES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val iTunesService = retrofit.create(iTunesApi::class.java)
}