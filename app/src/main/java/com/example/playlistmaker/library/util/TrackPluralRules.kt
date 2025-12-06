package com.example.playlistmaker.library.util

import java.util.Locale

interface TrackPluralRules {
    fun getEnding(count: Int): String
}

class EnglishTrackPluralRules : TrackPluralRules {
    override fun getEnding(count: Int): String {
        return if (count == 1) "track" else "tracks"
    }
}

class BelarusianTrackPluralRules : TrackPluralRules {
    override fun getEnding(count: Int): String {
        val lastDigit = count % 10
        val lastTwoDigits = count % 100
        return if (lastTwoDigits in 11..19) "трэкаў" else when (lastDigit) {
            1 -> "трэк"
            2, 3, 4 -> "трэкa"
            else -> "трэкаў"
        }
    }
}

class RussianTrackPluralRules : TrackPluralRules {
    override fun getEnding(count: Int): String {
        val lastDigit = count % 10
        val lastTwoDigits = count % 100
        return if (lastTwoDigits in 11..19) "треков" else when (lastDigit) {
            1 -> "трек"
            2, 3, 4 -> "трека"
            else -> "треков"
        }
    }
}

object TrackPluralizerFactory {
    fun getRules(locale: Locale = Locale.getDefault()): TrackPluralRules {
        return when (locale.language) {
            "be" -> BelarusianTrackPluralRules()
            "ru" -> RussianTrackPluralRules()
            else -> EnglishTrackPluralRules()
        }
    }
}