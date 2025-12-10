package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.library.ui.playlists.PlaylistAdapter
import com.example.playlistmaker.search.domain.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.getValue

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AudioPlayerViewModel by viewModel()
    private val gson: Gson by inject()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED,
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    binding.overlay.visibility = View.VISIBLE
                    binding.overlay.alpha = 1f
                }

                BottomSheetBehavior.STATE_HIDDEN -> {
                    binding.overlay.alpha = 0f
                    binding.overlay.visibility = View.GONE
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            _binding?.overlay?.alpha = slideOffset.coerceIn(0f, 1f)
        }
    }

    private val playlistAdapter = PlaylistAdapter(
        onClick = { playlist -> viewModel.addToPlaylist(playlist) },
        isSmall = true
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val trackJson = requireArguments().getString("trackJson")
        val track = gson.fromJson(trackJson, Track::class.java)
        updateUI(track)

        viewModel.setCurrentTrack(track)

        binding.addToFavoritesButton.setOnClickListener { viewModel.onFavoriteClicked() }

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            binding.addToFavoritesButton.setImageResource(
                if (state.isFavorite) R.drawable.add_to_favorites_button_activ
                else R.drawable.add_to_favorites_button
            )

            binding.playButton.isEnabled = state.playerState != PlayerState.PREPARING
            binding.playButton.setImageResource(
                if (state.playerState == PlayerState.PLAYING) R.drawable.pause_button
                else R.drawable.play_button
            )

            binding.progressTrack.text =
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(state.currentPosition)
        }

        binding.playButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        viewModel.prepare(track.previewUrl)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        binding.rvPlaylists.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvPlaylists.adapter = playlistAdapter

        binding.addToPlaylistButton.setOnClickListener {
            viewModel.loadPlaylistsForBottomSheet()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.btnNewPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_audioPlayerFragment_to_newPlaylistFragment)
        }

        viewModel.playlistsForBottomSheet.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.update(playlists)
        }

        viewModel.addToPlaylistStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is AddStatus.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.added_to_playlist)} ${status.playlistName}",
                        Toast.LENGTH_LONG
                    ).show()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }

                is AddStatus.AlreadyExists -> {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.track_already_added)} ${status.playlistName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateUI(track: Track) {
        binding.tvSongName.text = track.trackName
        binding.tvBandName.text = track.artistName
        binding.tvSongDurationValue.text = track.trackTime
        binding.tvAlbumNameValue.text = track.collectionName
        binding.tvSongYearValue.text = track.releaseDate.orEmpty().take(4)
        binding.tvSongGenreValue.text = track.primaryGenreName
        binding.tvTrackCountryValue.text = track.country

        val artworkUrl = track.artworkUrl100.replaceAfterLast("/", "512x512bb.jpg")
        Glide.with(requireContext())
            .load(artworkUrl)
            .placeholder(R.drawable.album_card_312dp)
            .into(binding.ivAlbumCard)

        binding.progressTrack.text = "00:00"
        binding.playButton.isEnabled = false
    }

    override fun onDestroyView() {
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        super.onDestroyView()
        _binding = null
    }
}