package com.example.musicplayerapp.repositories

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.services.MusicService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context) {
    private var controller: MediaController? = null
    private var readyCallback: (() -> Unit)? = null
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var _currentIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle

    private var _songList = MutableStateFlow(listOf<Song>())
    val songList: StateFlow<List<Song>> = _songList

    private var _shuffleList = MutableStateFlow(listOf<Song>())
    val shuffleList: StateFlow<List<Song>> = _shuffleList


    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )

        val controllerFuture = androidx.media3.session.MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            controller = controllerFuture.get()
            _isPlaying.value = controller?.isPlaying ?: false
            readyCallback?.invoke()
            startPositionUpdates()
            controller?.addListener(object: Player.Listener{
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _playbackState.value = playbackState
                    if(playbackState == Player.STATE_READY){
                        _duration.value = controller?.duration ?: 0
                    }
                    else if(playbackState == Player.STATE_ENDED){
                        onSongEnded()
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    _currentPosition.value = newPosition.positionMs
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentPosition.value = controller?.currentPosition ?: 0L
                }

                // Update position thường xuyên (khi đang play)
                override fun onEvents(player: Player, events: Player.Events) {
                    if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                        events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED)) {
                        _currentPosition.value = player.currentPosition
                    }
                }
            })
        }, ContextCompat.getMainExecutor(context))
    }

    private fun startPositionUpdates() {
        scope.launch {

            while (true) {

                controller?.let {
                    if (it.isPlaying) {
                        _currentPosition.value = it.currentPosition
                    }
                }

                delay(500)
            }
        }
    }

    fun onReady(callback: (() -> Unit)){
        if(controller != null) callback()
        else readyCallback = callback
    }

    fun hasPlaylist(): Boolean{
        return player()?.mediaItemCount!! > 0
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int){
        _songList.value = songs
        val mediaItem = songs.map {
            MediaItem.fromUri(it.contentUri)
        }
        _currentIndex.value = startIndex
        controller?.apply{
            setMediaItems(mediaItem, startIndex, 0)
            prepare()
            play()
        }
    }

    fun playIndex(index: Int){
        _currentIndex.value = index
        controller?.seekTo(index, 0)
        controller?.play()
    }

    fun playNext(){
        _currentIndex.value =
            (_currentIndex.value + 1) % (
                    if(_isShuffle.value) _shuffleList.value.size
                    else _songList.value.size)
        controller?.seekTo(_currentIndex.value, 0)
        controller?.play()
    }

    fun playPrevious(){
        val listSize = if(isShuffle.value) _shuffleList.value.size else _songList.value.size
        _currentIndex.value = (_currentIndex.value - 1 + listSize) % listSize
        controller?.seekTo(_currentIndex.value, 0)
        controller?.play()
    }

    fun pause(){
        controller?.pause()
    }

    fun resume(){
        controller?.play()
    }

    fun seekTo(position: Long){
        controller?.seekTo(position)
    }

    fun togglePlayPause(){
        controller?.let {
            if(it.isPlaying) pause()
            else resume()
        }
    }

    fun onSongEnded(){
        if(_isRepeat.value){
            playIndex(_currentIndex.value)
        }
        else{
            playNext()
        }
    }

    fun player(): Player? = controller
}