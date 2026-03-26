package com.lifecalendar

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lifecalendar.databinding.ActivityLanguageSelectionBinding
import java.util.Locale

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)

        binding.btnEnglish.setOnClickListener {
            selectLanguage("en")
        }

        binding.btnSpanish.setOnClickListener {
            selectLanguage("es")
        }
    }

    private fun selectLanguage(language: String) {
        prefsManager.saveLanguage(language)
        prefsManager.setFirstLaunchComplete()

        // Apply locale
        applyLocale(language)

        // Launch MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun applyLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun attachBaseContext(newBase: Context) {
        // Don't apply saved locale here — this is the language chooser
        super.attachBaseContext(newBase)
    }
}
