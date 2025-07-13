package com.example.playlistmaker.presentation.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.net.toUri
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.use_case.GetCurrentThemeUseCase
import com.example.playlistmaker.domain.use_case.SwitchThemeUseCase
import com.google.android.material.switchmaterial.SwitchMaterial

private lateinit var switchThemeUseCase: SwitchThemeUseCase
private lateinit var getCurrentThemeUseCase: GetCurrentThemeUseCase

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        switchThemeUseCase = Creator.provideSwitchThemeUseCase(context = this)
        getCurrentThemeUseCase = Creator.provideGetCurrentThemeUseCase(context = this)

        val backButton: Button = findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        val buttonShareApp = findViewById<Button>(R.id.button_share_app)
        val buttonWriteToSupport = findViewById<Button>(R.id.button_write_to_support)
        val buttonArrowForward = findViewById<Button>(R.id.button_arrow_forward)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        themeSwitcher.isChecked = getCurrentThemeUseCase.execute()
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            switchThemeUseCase.execute(isChecked)
            (applicationContext as App).switchTheme(isChecked)
        }

        buttonShareApp.setOnClickListener {
            val sharedText = getString(R.string.shared_text_in_button_share_apk)
            val titleShareScreen = getString(R.string.title_share_screen_in_button_share_apk)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, sharedText)
            }
            startActivity(Intent.createChooser(/* target = */ shareIntent, /* title = */ titleShareScreen))
        }

        buttonWriteToSupport.setOnClickListener{
            val recipientEmail = getString(R.string.recipient_email_in_button_write_to_support)
            val subject = getString(R.string.subject_in_button_write_to_support)
            val message = getString(R.string.message_in_button_write_to_support)

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$recipientEmail".toUri()
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(emailIntent)
        }

        buttonArrowForward.setOnClickListener {
            val url = getString(R.string.url_in_button_arrow_forward)

            val arrowForwardIntent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
            }
            startActivity(arrowForwardIntent)
        }
    }
}