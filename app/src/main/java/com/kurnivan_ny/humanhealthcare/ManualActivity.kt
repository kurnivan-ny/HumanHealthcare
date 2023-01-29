package com.kurnivan_ny.humanhealthcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import androidx.core.content.ContentProviderCompat.requireContext
import com.kurnivan_ny.humanhealthcare.databinding.ActivityManualBinding
import com.kurnivan_ny.humanhealthcare.utils.Preferences

class ManualActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManualBinding

    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = Preferences(this)

        binding.tvWaktuMakan.text = preferences.getValuesString("waktu_makan")

        binding.svMakanan.isFocusable = false

        binding.svMakanan.setOnClickListener {
            val intent = Intent(this, SearchMakananActivity::class.java)
            startActivity(intent)
        }

    }
}