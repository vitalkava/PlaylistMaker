package com.example.playlistmaker.library.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.library.ui.favorites.FavoritesFragment
import com.example.playlistmaker.library.ui.playlists.PlaylistsFragment

class LibraryPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    companion object {
        private const val TAB_COUNT = 2
    }

    enum class TabType(val position: Int, val fragmentCreator: () -> Fragment) {
        FAVORITES(0, { FavoritesFragment() }),
        PLAYLISTS(1, { PlaylistsFragment() })
    }

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return TabType.entries.find { it.position == position }?.fragmentCreator?.invoke()
            ?: FavoritesFragment()
    }
}