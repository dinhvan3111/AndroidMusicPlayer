package com.example.musicplayerapp.ui

import android.content.ContentUris
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.example.musicplayerapp.databinding.ActivityPlayerBinding
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.ui.viewmodels.PlayerViewModel
import com.example.musicplayerapp.utils.AlbumHelper
import com.example.musicplayerapp.utils.Utils
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var player : ExoPlayer
    private var handler: Handler = Handler()
    private lateinit var viewModel : PlayerViewModel
    private var isUserSeeking : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val songList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("songList", Song::class.java)
        } else {
            intent.getParcelableArrayListExtra("songList")
        }
        val currentIndex = intent.getIntExtra("position", 0)
        if(songList.isNullOrEmpty()){
            Toast.makeText(this,"No Songs Found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        viewModel = PlayerViewModel(songList, currentIndex)
        setupUIScreen()

    }

    private fun setupUIScreen(){
        setUpSeekBar()
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.favBtn.setOnClickListener {

        }
        binding.playBtnImage.setOnClickListener {
            if(player.isPlaying) player.pause() else player.play()
            updatePlayPauseButtonIcon()
        }
        binding.btnSkip.setOnClickListener {
            playNext()
        }
        binding.btnPrevious.setOnClickListener {
            playPrevious()
        }
        binding.btnShuffle.setOnClickListener {
            toggleShuffle()
        }
        binding.btnRepeatOnce.setOnClickListener {
            toggleRepeat()
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isShuffleFlow.collect { isShuffle ->
                        if(isShuffle){
                            binding.btnShuffle.setColorFilter(
                                    ContextCompat.getColor(
                                        this@PlayerActivity,
                                        R.color.purple)
                                )
                        }
                        else{
                            binding.btnShuffle.clearColorFilter()
                        }
                    }
                }
                launch {
                    viewModel.isRepeatFlow.collect { isRepeat ->
                        if(isRepeat){
                            binding.btnRepeatOnce.setColorFilter(
                                ContextCompat.getColor(
                                    this@PlayerActivity,
                                    R.color.purple)
                            )
                        }
                        else{
                            binding.btnRepeatOnce.clearColorFilter()
                        }
                    }
                }
            }
        }
        setUpSong()
    }

    private fun toggleRepeat() {
        viewModel.toggleRepeat()
    }

    private fun toggleShuffle() {
        viewModel.toggleShuffle()
        setUpSong()
    }

    private fun setUpSeekBar(){
        player = ExoPlayer.Builder(this).build()
        // Handle set Seekbar max duration
        player.addListener(object: Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayPauseButtonIcon()
                if(playbackState == Player.STATE_READY){
                    binding.seekBar.max = player.duration.toInt()
                    binding.txtSongDuration.text = Utils.formatDurationTime(
                        (player.duration / 1000).toInt()
                    )
                }
                else if(playbackState == Player.STATE_ENDED){
                    if(viewModel.isRepeatValue){
                        setUpSong()
                    }else{
                        playNext()
                    }
                }
            }
        })

        // Handle user drag seekbar
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if(fromUser){
                    player.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                isUserSeeking = false
            }

        })
    }

    private fun setUpSong(){
        val initSong = viewModel.songList.value[viewModel.currentIndexValue]
        binding.txtSongTitle.text = initSong.title
        binding.txtSongArtist.text = initSong.artist
        val albumArtUri = ContentUris.withAppendedId("content://media/external/audio/albumart".toUri(), initSong.albumId)
        if(AlbumHelper.hasAlbumArt(this,albumArtUri)) {
            Glide.with(this)
                .asBitmap()
                .load(albumArtUri)
                .circleCrop()
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(binding.imageAlbumArtPlayer)

            Glide.with(this)
                .asBitmap()
                .load(albumArtUri)
                .circleCrop()
                .transform(BlurTransformation(25,3))
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(binding.bgAlbumArt)
        }
        else{
            binding.imageAlbumArtPlayer.setImageResource(R.drawable.ic_music_note)
            binding.bgAlbumArt.setImageResource(R.drawable.ic_music_note)
        }
        val mediaItem = MediaItem.fromUri(initSong.contentUri)
        player.setMediaItem(mediaItem)
        player.prepare()

        // Auto update seek bar when song is playing
        updateSeekBar()

        player.play()

    }

    private fun updateSeekBar(){
        lifecycleScope.launch {
            while (true){
                // If user is dragging seekbar, do nothing
                if(!isUserSeeking){
                    binding.seekBar.progress = player.currentPosition.toInt()
                    binding.txtCurrentTime.text = Utils.formatDurationTime(
                        (player.currentPosition / 1000).toInt()
                    )
                }
                delay(500)
            }
        }
    }

    private fun updatePlayPauseButtonIcon(){
        binding.playBtnImage.setImageResource(
            if(player.isPlaying) R.drawable.ic_baseline_pause
            else R.drawable.ic_baseline_play_arrow
        )
    }

    private fun playNext(){
        viewModel.playNext()
        setUpSong()
    }

    private fun playPrevious(){
        viewModel.playPrevious()
        setUpSong()
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}