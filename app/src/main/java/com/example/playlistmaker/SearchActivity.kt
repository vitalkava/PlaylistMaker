package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    private val tracks = ArrayList<Track>()
    private val adapter = SearchAdapter({ track -> saveToHistory(track)})
    private val historyAdapter = SearchAdapter { track -> saveToHistory(track) }
    private val searchHistory by lazy { SearchHistory(this) }

    private var editTextValue: String = DEF_VALUE

    companion object {
        const val SEARCH_QUERY = "SEARCH_QUERY"
        const val DEF_VALUE = ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY, editTextValue)
        outState.putString("TRACKS_JSON", Gson().toJson(tracks))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        editTextValue = savedInstanceState.getString(SEARCH_QUERY, DEF_VALUE)
        val editText: EditText = findViewById(R.id.search)
        editText.setText(editTextValue)
        val tracksJson = savedInstanceState.getString("TRACKS_JSON", "[]")
        val restoredTracks: List<Track> = Gson().fromJson(tracksJson, Array<Track>::class.java).toList()
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

        recyclerView = findViewById(R.id.search_results)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        historyRecyclerView = findViewById(R.id.search_history_results)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        clearIcon = findViewById(R.id.clear_icon)
        queryInput = findViewById(R.id.search)
        searchHistoryView = findViewById(R.id.search_history)

        clearIcon.setOnClickListener {
            queryInput.setText("")
            tracks.clear()
            noResults.visibility = View.INVISIBLE
            noInternet.visibility = View.INVISIBLE
            adapter.updateData(tracks)
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(queryInput.windowToken, 0)
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
            searchHistory.clearHistory()
            showHistory()
        }

        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                editTextValue = s.toString()
                clearIcon.isVisible = !s.isNullOrEmpty()

                searchHistoryView.visibility = if (queryInput.text.isEmpty() && queryInput.hasFocus()) View.VISIBLE else View.GONE

                val filteredTracks = tracks.filter {
                    it.trackName.contains(editTextValue, ignoreCase = true) ||
                            it.artistName.contains(editTextValue, ignoreCase = true)
                }

                (recyclerView.adapter as SearchAdapter).apply {
                    adapter.updateData(filteredTracks)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        queryInput.addTextChangedListener(searchTextWatcher)

        queryInput.setOnFocusChangeListener { _, hasFocus ->
            val isHistoryVisible = hasFocus && queryInput.text.isEmpty() && searchHistory.getHistory().isNotEmpty()
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

    private fun performSearch(query: String) {
        hidePlaceholders()
        RetrofitClient.iTunesService.search(query).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful) {
                    val resultTracks = response.body()?.results ?: listOf()
                    if (resultTracks.isNotEmpty()) {
                        tracks.clear()
                        tracks.addAll(resultTracks)
                        adapter.updateData(tracks)
                    } else {
                        adapter.updateData(emptyList())
                        noResults.visibility = View.VISIBLE
                    }
                } else {
                    adapter.updateData(emptyList())
                    noResults.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                adapter.updateData(emptyList())
                noInternet.visibility = View.VISIBLE
            }
        })
    }

    private fun hidePlaceholders() {
        noResults.visibility = View.GONE
        noInternet.visibility = View.GONE
    }

    private fun saveToHistory(track: Track) {
        searchHistory.saveTrack(track)
        showHistory()
    }

    private fun showHistory() {
        val history = searchHistory.getHistory()
        historyAdapter.updateData(history)
        searchHistoryView.visibility = if (history.isNotEmpty()) View.VISIBLE else View.GONE
    }
}