package com.example.musicplayerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.musicplayerapp.MainActivity
import com.example.musicplayerapp.R
import com.example.musicplayerapp.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            delay(2000)
            goToMainActivity()
        }


//        binding.spBtn.setOnClickListener{
//            val mainActivity = Intent(this@SplashActivity, MainActivity::class.java)
//            startActivity(mainActivity)
//        }
    }
    private fun goToMainActivity(){
        val mainActivity = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(mainActivity)
    }
}