package com.example.playlistmaker.library.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistId = requireArguments().getLong("playlistId")

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.loadPlaylist(playlistId)

        viewModel.playlistState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistState.Loading -> {
                    // progress?????????????????????
                }

                is PlaylistState.Content -> {
                    bindPlaylist(state.playlist, state.tracks)
                }

                is PlaylistState.Empty -> {
                    // notFound???????
                }
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

            val trackCount = tracks.size
            tvCountOfTracks.text = resources.getQuantityString(
                R.plurals.tracks_count,
                trackCount,
                trackCount
            )
            val totalMillis = tracks.sumOf { it.trackTimeMillis }  // Long + Long
            val totalMinutes = SimpleDateFormat(
                "mm", Locale.getDefault()
            ).format(
                totalMillis
            )

            tvCountOfMinutes.text = resources.getQuantityString(
                R.plurals.minute_count,
                totalMinutes.toInt(),
                totalMinutes.toInt()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}