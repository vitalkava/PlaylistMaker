package com.example.playlistmaker.presentation.ui.main

import android.content.Intent
import android.os.Bundle
//import android.view.View
import android.widget.Button
//import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.presentation.ui.library.LibraryActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.ui.settings.SettingsActivity
import com.example.playlistmaker.presentation.ui.tracks.SearchActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSearch = findViewById<Button>(R.id.button_search)
        val buttonLibrary = findViewById<Button>(R.id.button_library)
        val buttonSettings = findViewById<Button>(R.id.button_settings)

        buttonSearch.setOnClickListener {
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
        }

        buttonLibrary.setOnClickListener {
            val libraryIntent = Intent(this, LibraryActivity::class.java)
            startActivity(libraryIntent)
        }

        buttonSettings.setOnClickListener {
            val settingIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingIntent)
        }

//        val buttonClickListener: View.OnClickListener = object : View.OnClickListener {
//            override fun onClick(v: View?) {
//                Toast.makeText(
//                    this@MainActivity,
//                    "This button will someday trigger a search :)",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//        buttonSearch.setOnClickListener(buttonClickListener)

//        buttonLibrary.setOnClickListener {
//            Toast.makeText(
//                this@MainActivity,
//                "This button will someday trigger the library of music :)",
//                Toast.LENGTH_SHORT
//            ).show()
//        }

//        buttonSettings.setOnClickListener {
//            Toast.makeText(
//                this@MainActivity,
//                "This button will someday trigger the settings :)",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }
}