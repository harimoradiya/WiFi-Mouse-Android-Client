package com.harimoradiya.wifimouseclientandroid.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.harimoradiya.wifimouseclientandroid.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.settingsToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}