package com.example.playlistmaker.library.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistItemBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.util.TrackPluralizerFactory

class PlaylistAdapter(private val onClick: (Playlist) -> Unit) :
    RecyclerView.Adapter<PlaylistViewHolder>() {

    private val playlists = mutableListOf<Playlist>()

    fun update(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = PlaylistItemBinding
            .inflate(
                LayoutInflater
                    .from(parent.context), parent, false
            )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position], onClick)
    }

    override fun getItemCount() = playlists.size
}

class PlaylistViewHolder(private val binding: PlaylistItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val pluralRules = TrackPluralizerFactory.getRules()
    fun bind(playlist: Playlist, onClick: (Playlist) -> Unit) {
        binding.playlistName.text = playlist.name
        binding.countOfTracks.text =
            "${playlist.trackCount} ${pluralRules.getEnding(playlist.trackCount)}"

        Glide.with(itemView)
            .load(playlist.coverUri ?: R.drawable.album_card_104)
            .into(binding.card)

        itemView.setOnClickListener { onClick(playlist) }
    }
}