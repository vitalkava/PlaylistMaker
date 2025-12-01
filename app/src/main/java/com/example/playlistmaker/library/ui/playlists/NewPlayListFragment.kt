package com.example.playlistmaker.library.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPlaylistViewModel by viewModel()

    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.pickCover(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            viewModel.handleBack()
        }

        binding.addName.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.updateName(text.toString())
        }

        binding.addDescription.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.updateDescription(text.toString())
        }

        binding.addCover.setOnClickListener {
            photoPicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        binding.createButton.setOnClickListener {
            viewModel.createPlaylist()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.createButton.isEnabled = state.isCreateEnabled
            if (state.coverUri != null) {
                binding.addCover.setImageURI(state.coverUri)
                binding.addCover.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NewPlaylistEvent.Created -> {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.playlist_)} ${event.name} ${getString(R.string._created)}",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().popBackStack()
                }

                NewPlaylistEvent.ShowConfirmDialog -> showConfirmDialog()

                NewPlaylistEvent.NavigateBack -> findNavController().popBackStack()

            }
        }
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(requireContext().getString(R.string.complete_playlist_creation_question))
            .setMessage(R.string.all_unsaved_data_will_be_lost)
            .setPositiveButton(R.string.complete_answer) { _, _ -> findNavController().popBackStack() }
            .setNegativeButton(R.string.cancel_answer) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NewPlaylistFragment()
    }
}