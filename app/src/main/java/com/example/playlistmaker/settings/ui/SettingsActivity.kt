package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.presentation.ui.settings.App

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            Creator.provideGetCurrentThemeUseCase(this),
            Creator.provideSwitchThemeUseCase(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }

        viewModel.isDarkTheme.observe(this) { isDark ->
            binding.themeSwitcher.isChecked = isDark
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTheme(isChecked)
            (applicationContext as App).switchTheme(isChecked)
        }

        binding.buttonShareApp.setOnClickListener {
            val text = getString(R.string.shared_text_in_button_share_apk)
            val title = getString(R.string.title_share_screen_in_button_share_apk)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(intent, title))
        }

        binding.buttonWriteToSupport.setOnClickListener {
            val email = getString(R.string.recipient_email_in_button_write_to_support)
            val subject = getString(R.string.subject_in_button_write_to_support)
            val message = getString(R.string.message_in_button_write_to_support)

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$email".toUri()
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(intent)
        }

        binding.buttonArrowForward.setOnClickListener {
            val url = getString(R.string.url_in_button_arrow_forward)
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
    }
}
