package com.example.playlistmaker.library.ui.playlists

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.model.Playlist
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : NewPlaylistFragment() {

    override val viewModel: EditPlaylistViewModel by viewModel()

    private val gson: Gson by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistJson = arguments?.getString("playlist_json")
        val playlist = playlistJson?.let { gson.fromJson(it, Playlist::class.java) }

        if (playlist == null) {
            findNavController().popBackStack()
            return
        }

        viewModel.loadPlaylistForEdit(playlist)

        binding.title.text = getString(R.string.edit)
        binding.createButton.text = getString(R.string.save)

        binding.createButton.setOnClickListener {
            viewModel.saveEditedPlaylist()
        }

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NewPlaylistEvent.Created -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.playlist_updated, event.name),
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().popBackStack(R.id.playlistFragment, false)
                }

                else -> findNavController().popBackStack()
            }
        }
    }
}