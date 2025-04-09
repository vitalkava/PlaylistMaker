package com.example.playlistmaker

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class SearchAdapter(
    private val tracks: List<Track>
): RecyclerView.Adapter<SearchResultViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_result_view, parent, false)
        return SearchResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}

class SearchResultViewHolder(itemView: View): ViewHolder(itemView) {

    private val albumImage: ImageView = itemView.findViewById(R.id.ivAlbumCard)
    private val trackName: TextView = itemView.findViewById(R.id.tvSongName)
    private val artistName: TextView = itemView.findViewById(R.id.tvBandName)
    private val songDuration: TextView = itemView.findViewById(R.id.tvSongDuration)

    fun bind(track: Track) {
        trackName.text = track.trackName
        artistName.text = track.artistName
        songDuration.text = track.trackTime

        Glide.with(itemView.context).load(track.artworkUrl100).placeholder(R.drawable.album_card_image).transform(RoundedCorners(dpToPx(2f, itemView.context))).into(albumImage)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics).toInt()
    }
}