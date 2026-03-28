package com.example.musicplayerapp.ui

import android.content.ContentUris
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.ui.adapters.DragCallBack
import com.example.musicplayerapp.ui.adapters.IOnStartDragListener
import com.example.musicplayerapp.ui.adapters.SongAdapter
import com.example.musicplayerapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var songAdapter: SongAdapter
    private val viewModel : PlayerViewModel by viewModels()
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
        viewModel.initSongList(songList, currentIndex)
        setupUIScreen()

    }

    private fun setupUIScreen(){
        setUpListSongRV()
        setUpSeekBar()
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.favBtn.setOnClickListener {

        }
        binding.playBtnImage.setOnClickListener {
            viewModel.togglePlayPause()
        }
        binding.btnSkip.setOnClickListener {
            viewModel.playNext()
        }
        binding.btnPrevious.setOnClickListener {
            viewModel.playPrevious()
        }
        binding.btnShuffle.setOnClickListener {
            viewModel.toggleShuffle()
        }
        binding.btnRepeatOnce.setOnClickListener {
            viewModel.toggleRepeat()
        }
        binding.btnShowListSong.setOnClickListener{
            val motion = binding.main
            if(motion.currentState == R.id.start){
                motion.transitionToState(R.id.end)
                binding.btnShowListSong.background =
                    ContextCompat.getDrawable(this, R.drawable.rounded_bg_grey)
            }
            else{
                motion.transitionToState(R.id.start)
                binding.btnShowListSong.background = ColorDrawable(Color.TRANSPARENT)
            }
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

    private fun setUpListSongRV(){
        lateinit var touchHelper: ItemTouchHelper;
        songAdapter = SongAdapter(object : IOnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper.startDrag(viewHolder)
            }
        }, true, 50).apply {
            setOnItemClickListener{selectedSong -> onItemClick(selectedSong)}
        }
        binding.rvListSong.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(context)
        }
        val dragCallBack = DragCallBack(songAdapter)
        touchHelper = ItemTouchHelper(dragCallBack)
        touchHelper.attachToRecyclerView(binding.rvListSong)
        lifecycleScope.launch {
            viewModel.songList.collect() { list ->
                songAdapter.differ.submitList(list)
            }
        }
    }

    private fun setUpSeekBar(){
        // Handle set Seekbar max duration
        updatePlayPauseButtonIcon()

        // Handle user drag seekbar
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if(fromUser){
                    viewModel.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                viewModel.userStartTouch()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                viewModel.userStopTouch()
            }

        })
    }

    private fun setUpSong(){
        // Auto update seek bar when song is playing
        updateSeekBar()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.currentSong.collect { song ->
                    binding.txtSongTitle.text = song?.title
                    binding.txtSongArtist.text = song?.artist
                    val albumArtUri = ContentUris.withAppendedId("content://media/external/audio/albumart".toUri(), song?.albumId ?: 0L)
                    if(AlbumHelper.hasAlbumArt(this@PlayerActivity,albumArtUri)) {
                        Glide.with(this@PlayerActivity)
                            .asBitmap()
                            .load(albumArtUri)
                            .circleCrop()
                            .placeholder(R.drawable.ic_music_note)
                            .error(R.drawable.ic_music_note)
                            .into(binding.imageAlbumArtPlayer)

                        Glide.with(this@PlayerActivity)
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
                }
            }
        }
    }

    private fun updateSeekBar(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.currentPosition.collect { currentPosition ->
                        if(!viewModel.isUserSeeking){
                            binding.seekBar.progress = currentPosition.toInt()
                            binding.txtCurrentTime.text = Utils.formatDurationTime(
                                (currentPosition / 1000).toInt()
                            )
                        }
                    }
                }
                launch {
                    viewModel.duration.collect { duration ->
                        binding.seekBar.max = duration.toInt()
                        binding.txtSongDuration.text = Utils.formatDurationTime(
                            (duration / 1000).toInt()
                        )
                    }
                }
            }
        }
    }

    private fun updatePlayPauseButtonIcon(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isPlaying.collect { isPlaying ->
                    binding.playBtnImage.setImageResource(
                        if(isPlaying) R.drawable.ic_baseline_pause
                        else R.drawable.ic_baseline_play_arrow
                    )
                }
            }
        }
    }

    private fun onItemClick(selectedSong: Song){
        val index = songAdapter.differ.currentList.indexOf(selectedSong)
        viewModel.playSong(index)
    }

    override fun onStop() {
        super.onStop()
//        player?.release()
    }
}