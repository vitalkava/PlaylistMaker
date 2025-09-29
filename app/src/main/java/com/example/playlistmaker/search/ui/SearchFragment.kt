package com.example.playlistmaker.search.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.player.ui.AudioPlayerFragment
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchFragment: Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModel()
    private val gson: Gson by inject()
    private val adapter: SearchAdapter by inject { parametersOf(::onTrackSelected) }
    private val historyAdapter: SearchAdapter by inject { parametersOf(::onTrackSelected) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResults.adapter = adapter

        binding.searchHistoryResults.layoutManager = LinearLayoutManager(requireContext())
        binding.searchHistoryResults.adapter = historyAdapter

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.isLoading
            if (state.isLoading) {
                binding.searchResults.isVisible = false
                binding.searchHistory.isVisible = false
                binding.noResults.isVisible = false
                binding.noInternet.isVisible = false
            } else {
                binding.searchResults.isVisible = state.tracks.isNotEmpty() && !state.isHistoryVisible
                binding.searchHistory.isVisible = state.isHistoryVisible
                binding.noResults.isVisible = state.isNoResultsVisible
                binding.noInternet.isVisible = state.isNoInternetVisible

                if (state.isHistoryVisible) {
                    historyAdapter.updateData(state.tracks)
                } else {
                    adapter.updateData(state.tracks)
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

    private fun onTrackSelected(track: Track) {
        viewModel.saveTrackToHistory(track)
        val trackJson = gson.toJson(track)
        val fragment = AudioPlayerFragment.newInstance(trackJson)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.rootFragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}