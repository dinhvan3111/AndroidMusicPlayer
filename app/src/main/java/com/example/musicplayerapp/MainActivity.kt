package com.example.musicplayerapp

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.musicplayerapp.databinding.ActivityMainBinding
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.ui.PlayerActivity
import com.example.musicplayerapp.ui.viewmodels.MainViewModel
import com.example.musicplayerapp.ui.viewmodels.PlayerViewModel
import com.example.musicplayerapp.utils.AlbumHelper
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()
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

        setupMiniPlayer()
    }

    private fun setupMiniPlayer(){
        binding.miniPlayer.root.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putParcelableArrayListExtra(
                "songList",
                ArrayList<Song>(viewModel.songList.value)
            )
            intent.putExtra("position", viewModel.currentIndex.value)
            startActivity(intent)
        }
        binding.miniPlayer.btnPlaySong.setOnClickListener {
            viewModel.togglePlayPause()
        }

        binding.miniPlayer.btnPlayNext.setOnClickListener {
            viewModel.playNext()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.currentSong.collect { song ->
                        if(song != null){
                            binding.miniPlayer.root.isVisible = true
                            binding.miniPlayer.txtSongName.text = song.title
                            binding.miniPlayer.textView2.text = song.artist
                            val albumArtUri = ContentUris.withAppendedId("content://media/external/audio/albumart".toUri(), song.albumId)
                            if(AlbumHelper.hasAlbumArt(this@MainActivity,albumArtUri)) {
                                Glide.with(this@MainActivity)
                                    .asBitmap()
                                    .load(albumArtUri)
                                    .transform(RoundedCorners(16))
                                    .placeholder(R.drawable.ic_music_note)
                                    .error(R.drawable.ic_music_note)
                                    .into(binding.miniPlayer.imageSong)

                                Glide.with(this@MainActivity)
                                    .asBitmap()
                                    .load(albumArtUri)
                                    .transform(RoundedCorners(16))
                                    .placeholder(R.drawable.ic_music_note)
                                    .error(R.drawable.ic_music_note)
                                    .into(binding.miniPlayer.imageSong)
                            }
                            else{
                                binding.miniPlayer.imageSong.setImageResource(R.drawable.ic_music_note)
                            }
                        }
                        else{
                            binding.miniPlayer.root.isVisible = false
                        }
                    }
                }
                launch {
                    viewModel.isPlaying.collect { isPlaying ->
                        binding.miniPlayer.btnPlaySong.setImageResource(
                            if(isPlaying) R.drawable.ic_baseline_pause
                            else R.drawable.ic_baseline_play_arrow
                        )
                    }
                }
            }
        }
    }
}