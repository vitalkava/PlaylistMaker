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

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> FavoritesFragment.newInstance()
            1 -> PlaylistsFragment.newInstance()
            else -> FavoritesFragment.newInstance()
        }
    }
}