package com.example.playlistmaker.search.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.player.ui.AudioPlayerActivity
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            Creator.provideTracksInteractor(),
            Creator.provideSearchHistoryInteractor(this)
        )
    }

    private val adapter = SearchAdapter(::onTrackSelected)
    private val historyAdapter = SearchAdapter(::onTrackSelected)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.searchResults.layoutManager = LinearLayoutManager(this)
        binding.searchResults.adapter = adapter

        binding.searchHistoryResults.layoutManager = LinearLayoutManager(this)
        binding.searchHistoryResults.adapter = historyAdapter

        viewModel.screenState.observe(this) { state ->
            when (state) {
                is SearchScreenState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.searchResults.isVisible = false
                    binding.searchHistory.isVisible = false
                    binding.noResults.isVisible = false
                    binding.noInternet.isVisible = false
                }
                is SearchScreenState.SearchResults -> {
                    adapter.updateData(state.tracks)
                    binding.searchResults.isVisible = true
                    binding.searchHistory.isVisible = false
                    binding.noResults.isVisible = false
                    binding.noInternet.isVisible = false
                    binding.progressBar.isVisible = false
                }
                is SearchScreenState.History -> {
                    historyAdapter.updateData(state.tracks)
                    binding.searchHistory.isVisible = true
                    binding.searchResults.isVisible = false
                    binding.noResults.isVisible = false
                    binding.noInternet.isVisible = false
                    binding.progressBar.isVisible = false
                }
                is SearchScreenState.NoResults -> {
                    binding.noResults.isVisible = true
                    binding.searchResults.isVisible = false
                    binding.searchHistory.isVisible = false
                    binding.noInternet.isVisible = false
                    binding.progressBar.isVisible = false
                }
                is SearchScreenState.NoInternet -> {
                    binding.noInternet.isVisible = true
                    binding.searchResults.isVisible = false
                    binding.searchHistory.isVisible = false
                    binding.noResults.isVisible = false
                    binding.progressBar.isVisible = false
                }
                is SearchScreenState.Empty -> {
                    binding.searchResults.isVisible = false
                    binding.searchHistory.isVisible = false
                    binding.noResults.isVisible = false
                    binding.noInternet.isVisible = false
                    binding.progressBar.isVisible = false
                }
            }
        }

        binding.searchInput.addTextChangedListener { s: CharSequence? ->
            val text = s.toString()
            viewModel.onQueryChanged(text)
            binding.clearIcon.isVisible = text.isNotEmpty()
        }

        binding.clearIcon.setOnClickListener {
            binding.searchInput.text?.clear()
            binding.clearIcon.isVisible = false
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.buttonRefresh.setOnClickListener {
            viewModel.retrySearch()
        }

        val currentText = savedInstanceState?.getString("SEARCH_QUERY") ?: binding.searchInput.text?.toString().orEmpty()
        binding.searchInput.setText(currentText)
        viewModel.onQueryChanged(currentText)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_QUERY", binding.searchInput.text?.toString())
    }

    private fun onTrackSelected(track: Track) {
        viewModel.saveTrackToHistory(track)
        startActivity(
            Intent(this, AudioPlayerActivity::class.java).apply {
                putExtra("TRACK_DATA", Gson().toJson(track))
            }
        )
    }
}