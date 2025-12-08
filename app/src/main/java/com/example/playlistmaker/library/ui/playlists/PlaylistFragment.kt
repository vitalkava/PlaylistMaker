package com.example.playlistmaker.library.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.ui.SearchAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModel()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val trackAdapter = SearchAdapter { track ->
        val json = Gson().toJson(track)
        val bundle = Bundle().apply { putString("trackJson", json) }
        findNavController().navigate(R.id.action_playlistFragment_to_audioPlayerFragment, bundle)
    }.apply {
        setOnLongClickListener { track ->
            showDeleteDialog(track)
            true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistId = requireArguments().getLong("playlistId")
        viewModel.loadPlaylist(playlistId)

        binding.buttonBack.setOnClickListener { findNavController().navigateUp() }

        binding.rvTracks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetTracks).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            isHideable = false
        }

        viewModel.playlistState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistState.Loading -> Unit
                is PlaylistState.Content -> {
                    bindPlaylist(state.playlist, state.tracks)
                    trackAdapter.updateData(state.tracks.reversed())
                    updateBottomSheetVisibility(state.tracks.isNotEmpty())
                }
                is PlaylistState.Empty -> Unit
            }
        }
    }

    private fun bindPlaylist(playlist: Playlist, tracks: List<Track>) {
        with(binding) {
            Glide.with(this@PlaylistFragment)
                .load(playlist.coverUri)
                .placeholder(R.drawable.album_card_312dp)
                .into(ivPlaylistCard)

            tvPlaylistName.text = playlist.name
            tvPlaylistDescription.text = playlist.description ?: ""
            tvPlaylistDescription.visibility =
                if (playlist.description.isNullOrBlank()) View.GONE else View.VISIBLE

            tvCountOfTracks.text = resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.trackCount,
                playlist.trackCount
            )

            val totalMillis = tracks.sumOf { it.trackTimeMillis }
            val minutes = totalMillis / 60000
            tvCountOfMinutes.text = resources.getQuantityString(
                R.plurals.minute_count,
                minutes.toInt(),
                minutes.toInt()
            )
        }
    }

    private fun updateBottomSheetVisibility(hasTracks: Boolean) {
        bottomSheetBehavior.state = if (hasTracks) {
            BottomSheetBehavior.STATE_COLLAPSED
        } else {
            BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showDeleteDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.do_you_want_to_delete_the_track))
            .setNegativeButton(getString(R.string.yes)) { _, _ -> }
            .setPositiveButton(getString(R.string.no)) { _, _ ->
                viewModel.deleteTrackFromPlaylist(track)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}