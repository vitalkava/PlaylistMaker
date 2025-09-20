package com.example.playlistmaker.library.ui.playlists

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private val viewModel: PlaylistsViewModel by viewModel()
    private lateinit var binding: FragmentPlaylistsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPlaylistsBinding.inflate(layoutInflater)
        val playlists = viewModel.getPlaylists()
        val placeholder = binding.placeholder

        if (playlists.isEmpty()) {
            placeholder.visibility = View.VISIBLE
        } else {
            placeholder.visibility = View.GONE
        }

        binding.buttonNewPlaylist.setOnClickListener {
            // be later
        }
    }
}