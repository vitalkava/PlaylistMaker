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
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.player.ui.AudioPlayerActivity
import com.example.playlistmaker.presentation.ui.tracks.SearchAdapter
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            Creator.provideTracksInteractor(),
            Creator.provideSearchHistoryInteractor(this)
        )
    }

    private val adapter = SearchAdapter { onTrackSelected(it) }
    private val historyAdapter = SearchAdapter { onTrackSelected(it) }

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

        viewModel.searchResults.observe(this) { list ->
            adapter.updateData(list)
            binding.searchResults.isVisible = list.isNotEmpty()
            binding.noResults.isVisible =
                list.isEmpty() && !binding.searchInput.text.isNullOrEmpty()
        }
        viewModel.history.observe(this) { list ->
            historyAdapter.updateData(list)
            binding.searchHistory.isVisible =
                list.isNotEmpty() && binding.searchInput.text.isEmpty()
        }
        viewModel.isLoading.observe(this) {
            binding.progressBar.isVisible = it
        }
        viewModel.errorMessage.observe(this) { msg ->
            binding.noResults.isVisible = msg == "nothing_found"
            binding.noInternet.isVisible = msg == "no_internet"
        }

        binding.searchInput.addTextChangedListener { s: CharSequence? ->
            val text = s.toString()
            viewModel.onQueryChanged(text)

            binding.clearIcon.isVisible = text.isNotEmpty()

            binding.searchHistory.isVisible =
                viewModel.history.value?.isNotEmpty() == true && text.isEmpty()
        }

        binding.clearIcon.setOnClickListener {
            binding.searchInput.text?.clear()
            binding.clearIcon.isVisible = false
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        viewModel.loadHistory()

        binding.buttonRefresh.setOnClickListener {
            val currentText = binding.searchInput.text?.toString().orEmpty()
            if (currentText.isNotBlank()) {
                viewModel.retrySearch()
            }
        }
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