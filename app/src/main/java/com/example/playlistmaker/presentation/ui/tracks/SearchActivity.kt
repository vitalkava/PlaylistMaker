package com.example.playlistmaker.presentation.ui.tracks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.presentation.ui.player.AudioPlayerActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.use_case.FilterTracksUseCase
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    private lateinit var queryInput: EditText
    private lateinit var clearIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView

    private lateinit var noResults: View
    private lateinit var noInternet: View
    private lateinit var searchHistoryView: View
    private lateinit var buttonRefresh: Button
    private lateinit var buttonClearHistory: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tracksInteractor: TracksInteractor
    private lateinit var searchHistoryInteractor: SearchHistoryInteractor
    private lateinit var filterTracksUseCase: FilterTracksUseCase

    private val tracks = ArrayList<Track>()
    private val adapter = SearchAdapter { track -> saveToHistoryAndOpenPlayer(track) }
    private val historyAdapter = SearchAdapter { track -> saveToHistoryAndOpenPlayer(track) }
    private var isClickAllowed: Boolean = true
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(queryInput.text.toString().trim()) }

    private var editTextValue: String = DEF_VALUE

    companion object {
        const val SEARCH_QUERY = "SEARCH_QUERY"
        const val DEF_VALUE = ""
        const val TRACKS_JSON = "TRACKS_JSON"
        const val EMPTY_JSON_ARRAY = "[]"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY, editTextValue)
        outState.putString(TRACKS_JSON, Gson().toJson(tracks))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        editTextValue = savedInstanceState.getString(SEARCH_QUERY, DEF_VALUE)
        val editText: EditText = findViewById(R.id.search)
        editText.setText(editTextValue)
        val tracksJson = savedInstanceState.getString(TRACKS_JSON, EMPTY_JSON_ARRAY)
        val restoredTracks: List<Track> =
            Gson().fromJson(tracksJson, Array<Track>::class.java).toList()
        tracks.clear()
        tracks.addAll(restoredTracks)
        adapter.updateData(tracks)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton: Button = findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        tracksInteractor = Creator.provideTracksInteractor()
        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)
        filterTracksUseCase = Creator.provideFilterTracksUseCase()

        recyclerView = findViewById(R.id.search_results)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        historyRecyclerView = findViewById(R.id.search_history_results)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        clearIcon = findViewById(R.id.clear_icon)
        queryInput = findViewById(R.id.search)
        searchHistoryView = findViewById(R.id.search_history)
        progressBar = findViewById(R.id.progressBar)

        clearIcon.setOnClickListener {
            queryInput.setText("")
            tracks.clear()
            noResults.visibility = View.INVISIBLE
            noInternet.visibility = View.INVISIBLE
            adapter.updateData(tracks)
            hideKeyboard()
        }

        noResults = findViewById(R.id.no_results)
        noInternet = findViewById(R.id.no_internet)
        buttonRefresh = findViewById(R.id.button_refresh)
        buttonClearHistory = findViewById(R.id.button_clear_history)

        buttonRefresh.setOnClickListener {
            val query = queryInput.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }

        buttonClearHistory.setOnClickListener {
            searchHistoryInteractor.clearHistory()
            showHistory()
        }

        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                searchDebounce()
                editTextValue = s.toString()
                clearIcon.isVisible = !s.isNullOrEmpty()
                searchHistoryView.isVisible = queryInput.text.isEmpty() && queryInput.hasFocus()
                val filteredTracks = filterTracksUseCase.execute(tracks, editTextValue)
                adapter.updateData(filteredTracks)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        }

        queryInput.addTextChangedListener(searchTextWatcher)

        queryInput.setOnFocusChangeListener { _, hasFocus ->
            val isHistoryVisible =
                hasFocus && queryInput.text.isEmpty() && searchHistoryInteractor.getHistory().isNotEmpty()
            searchHistoryView.visibility = if (isHistoryVisible) View.VISIBLE else View.GONE
        }

        queryInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = queryInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }
        showHistory()
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun performSearch(query: String) {
        hidePlaceholders()
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (foundTracks.isNotEmpty()) {
                        recyclerView.visibility = View.VISIBLE
                        tracks.clear()
                        tracks.addAll(foundTracks)
                        adapter.updateData(tracks)
                    } else {
                        adapter.updateData(emptyList())
                        noResults.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun hidePlaceholders() {
        noResults.visibility = View.GONE
        noInternet.visibility = View.GONE
    }

    private fun saveToHistoryAndOpenPlayer(track: Track) {

        if (clickDebounce()) {
            searchHistoryInteractor.saveTrack(track)
            showHistory()

            val intent = Intent(this, AudioPlayerActivity::class.java).apply {
                putExtra("TRACK_DATA", Gson().toJson(track))
            }
            startActivity(intent)
        }
    }

    private fun showHistory() {
        val history = searchHistoryInteractor.getHistory()
        historyAdapter.updateData(history)
        searchHistoryView.isVisible = history.isNotEmpty() && queryInput.text.isEmpty()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}