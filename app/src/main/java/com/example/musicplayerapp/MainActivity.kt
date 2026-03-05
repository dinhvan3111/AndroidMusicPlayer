package com.example.musicplayerapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.musicplayerapp.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener { splashScreen ->
            splashScreen.remove()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        // Chỉ xử lý insets cho BottomNavigationView để nó không bị che bởi navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigationView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.musicNavHostFragment)
                as? NavHostFragment
        navHostFragment?.let {
            val navController = it.navController
            binding.bottomNavigationView.setupWithNavController(navController)
        }
    }
}