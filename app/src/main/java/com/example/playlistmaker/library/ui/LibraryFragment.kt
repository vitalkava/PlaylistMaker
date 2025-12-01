package com.example.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentLibraryBinding
import com.google.android.material.tabs.TabLayoutMediator

class LibraryFragment: Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private val tabTitles = listOf(R.string.favorite_tracks, R.string.playlists)
    private lateinit var tabsMediator: TabLayoutMediator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = LibraryPagerAdapter(
            fragmentManager = childFragmentManager,
            lifecycle = lifecycle,

        )

        tabsMediator = TabLayoutMediator(
            binding.libraryTabLayout,
            binding.viewPager
        ) { tab, position ->
            tab.text = getString(tabTitles[position])
        }
        tabsMediator.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabsMediator.detach()
        _binding = null
    }
}