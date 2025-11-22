package com.example.playlistmaker.library.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoritesBinding
import com.example.playlistmaker.search.ui.SearchAdapter
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModel()
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val adapter = SearchAdapter { track ->
        val bundle = Bundle().apply { putString("trackJson", Gson().toJson(track)) }
        findNavController().navigate(R.id.action_libraryFragment_to_audioPlayerFragment, bundle)
    }

    companion object {
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentFavoritesBinding.inflate(layoutInflater)

        binding.favoritesList.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesList.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesState.Empty -> {
                    binding.placeholder.visibility = View.VISIBLE
                    binding.favoritesList.visibility = View.GONE
                }

                is FavoritesState.Content -> {
                    binding.placeholder.visibility = View.GONE
                    binding.favoritesList.visibility = View.VISIBLE
                    adapter.updateData(state.tracks)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}