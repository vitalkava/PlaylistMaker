package com.example.playlistmaker.library.ui.playlists

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private lateinit var bottomSheetMenuBehavior: BottomSheetBehavior<*>
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

        bottomSheetMenuBehavior = BottomSheetBehavior.from(binding.bottomSheetMenu).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        val bottomSheetMenuCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.overlayMenu.visibility = View.VISIBLE
                        binding.overlayMenu.alpha = 1f
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlayMenu.alpha = 0f
                        binding.overlayMenu.visibility = View.GONE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlayMenu.alpha = slideOffset.coerceIn(0f, 1f)
            }
        }
        bottomSheetMenuBehavior.addBottomSheetCallback(bottomSheetMenuCallback)

        binding.buttonMenu.setOnClickListener {
            val state =
                viewModel.playlistState.value as? PlaylistState.Content ?: return@setOnClickListener
            bindMenuInfo(state.playlist)
            bottomSheetMenuBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.buttonMenuDelete.setOnClickListener {
            bottomSheetMenuBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }

        binding.buttonMenuEditInformation.setOnClickListener {
            bottomSheetMenuBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val state = viewModel
                .playlistState.value as? PlaylistState.Content ?: return@setOnClickListener

            val playlistJson = Gson().toJson(state.playlist)

            val bundle = Bundle().apply {
                putString("playlist_json", playlistJson)
            }

            findNavController().navigate(
                R.id.action_playlistFragment_to_editPlaylistFragment,
                bundle
            )
        }

        binding.buttonMenuShare.setOnClickListener {
            bottomSheetMenuBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            handleShareClick()
        }

        binding.buttonShare.setOnClickListener {
            handleShareClick()
        }

        viewModel.playlistState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistState.Loading -> Unit
                is PlaylistState.Content -> {
                    bindPlaylist(state.playlist, state.tracks)
                    trackAdapter.updateData(state.tracks)
                    updateBottomSheetVisibility()

                    if (state.tracks.isEmpty()) {
                        binding.playlistIsEmpty.visibility = View.VISIBLE
                        binding.rvTracks.visibility = View.GONE
                    } else {
                        binding.playlistIsEmpty.visibility = View.GONE
                        binding.rvTracks.visibility = View.VISIBLE
                    }
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

    private fun bindMenuInfo(playlist: Playlist) {
        Glide.with(this)
            .load(playlist.coverUri)
            .placeholder(R.drawable.album_card_104)
            .into(binding.card)

        binding.playlistName.text = playlist.name
        binding.countOfTracks.text = resources.getQuantityString(
            R.plurals.tracks_count,
            playlist.trackCount,
            playlist.trackCount
        )
    }

    private fun updateBottomSheetVisibility() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun showDeleteDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.do_you_want_to_delete_the_track))
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteTrackFromPlaylist(track)
            }
            .show()
    }

    private fun showDeletePlaylistDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.delete_playlist))
            .setMessage(getString(R.string.do_you_want_to_delete_the_playlist))
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val state = viewModel.playlistState.value as? PlaylistState.Content
                    ?: return@setPositiveButton
                viewModel.deletePlaylist(state.playlist.id) {
                    findNavController().navigateUp()
                }
            }
            .show()
    }

    private fun buildShareText(playlist: Playlist, tracks: List<Track>): String {
        val sb = StringBuilder()
        sb.append(playlist.name).append("\n")
        playlist.description?.let { sb.append(it).append("\n") }
        val trackCountText = resources.getQuantityString(
            R.plurals.tracks_count,
            playlist.trackCount,
            playlist.trackCount
        )
        sb.append(trackCountText).append("\n")
        tracks.forEachIndexed { index, track ->
            sb.append("${index + 1}. ${track.artistName} - ${track.trackName} (${track.trackTime})\n")
        }
        return sb.toString()
    }

    private fun sharePlaylist(playlist: Playlist, tracks: List<Track>) {
        val text = buildShareText(playlist, tracks)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_the_playlist)))
    }

    private fun handleShareClick() {
        val state = viewModel.playlistState.value as? PlaylistState.Content ?: return
        if (state.tracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_does_not_have_a_track_list),
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            sharePlaylist(state.playlist, state.tracks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}