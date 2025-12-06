package com.example.playlistmaker.library.ui.playlists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistItemBinding
import com.example.playlistmaker.databinding.PlaylistItemSmallBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.util.TrackPluralizerFactory

class PlaylistAdapter(
    private val onClick: (Playlist) -> Unit,
    private val isSmall: Boolean = false,
) :
    RecyclerView.Adapter<PlaylistViewHolder>() {

    private val playlists = mutableListOf<Playlist>()

    fun update(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return if (isSmall) {
            val binding = PlaylistItemSmallBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            PlaylistViewHolder(binding.root, true)
        } else {
            val binding = PlaylistItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            PlaylistViewHolder(binding.root, false)
        }
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position], onClick, isSmall)
    }

    override fun getItemCount() = playlists.size
}

class PlaylistViewHolder(
    itemView: View,
    private val isSmall: Boolean
) :
    RecyclerView.ViewHolder(itemView) {
    private val pluralRules = TrackPluralizerFactory.getRules()
    fun bind(playlist: Playlist, onClick: (Playlist) -> Unit, isSmall: Boolean) {
        if (isSmall) {
            val binding = PlaylistItemSmallBinding.bind(itemView)
            binding.playlistName.text = playlist.name
            binding.countOfTracks.text =
                "${playlist.trackCount} ${pluralRules.getEnding(playlist.trackCount)}"

            Glide.with(itemView)
                .load(playlist.coverUri ?: R.drawable.album_card_104)
                .into(binding.card)
        } else {
            val binding = PlaylistItemBinding.bind(itemView)
            binding.playlistName.text = playlist.name
            binding.countOfTracks.text =
                "${playlist.trackCount} ${pluralRules.getEnding(playlist.trackCount)}"

            Glide.with(itemView)
                .load(playlist.coverUri ?: R.drawable.album_card_104)
                .into(binding.card)
        }
        itemView.setOnClickListener { onClick(playlist) }
    }
}