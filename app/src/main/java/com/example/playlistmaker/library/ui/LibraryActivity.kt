package com.example.playlistmaker.library.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityLibraryBinding
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryActivity : AppCompatActivity() {

    private val tabTitles = listOf(R.string.favorite_tracks, R.string.playlists)
    private val viewModel: LibraryViewModel by viewModel()
    private lateinit var binding: ActivityLibraryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = LibraryPagerAdapter(this)

        TabLayoutMediator(
            binding.libraryTabLayout,
            binding.viewPager
        ) { tab, position ->
            tab.text = getString(tabTitles[position])
        }.attach()
    }
}