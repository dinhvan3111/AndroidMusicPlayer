package com.example.musicplayerapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.repositories.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
) : ViewModel() {
    private var _isUserSeeking : Boolean = false
     var isUserSeeking : Boolean = _isUserSeeking

    val duration: StateFlow<Long> = playerRepository.duration

    val isPlaying: StateFlow<Boolean> = playerRepository.isPlaying

    val playbackState: StateFlow<Int> = playerRepository.playbackState

    val currentPosition: StateFlow<Long> = playerRepository.currentPosition

    var songList: StateFlow<List<Song>> = playerRepository.songList

    var currentIndex : StateFlow<Int> = playerRepository.currentIndex

    val currentSong : StateFlow<Song?> =
        combine(songList, currentIndex) { songs, index ->
            songs.getOrNull(index)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )


    private var _shuffleList : MutableStateFlow<List<Song>> = MutableStateFlow(listOf<Song>())
    val shuffleList: StateFlow<List<Song>> = _shuffleList




    private var _isShuffle: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isShuffleFlow: StateFlow<Boolean> = _isShuffle
    var isShuffleValue : Boolean
        get() = _isShuffle.value
        set(value) { _isShuffle.value = value }

    private var _isRepeat: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRepeatFlow: StateFlow<Boolean> = _isRepeat
    var isRepeatValue : Boolean
        get() = _isRepeat.value
        set(value) { _isRepeat.value = value }

    fun getPlayer(): Player?{
        return playerRepository.player()
    }

    init {
        _shuffleList.value = ArrayList<Song>(songList.value)
    }

    fun initSongList(initSongList : List<Song>, initPosition: Int,){
        playerRepository.onReady {
            if (!playerRepository.hasPlaylist()) {
                playerRepository.setPlaylist(initSongList, initPosition)
            }
            else{
                if(playerRepository.currentIndex.value != initPosition)
                    playerRepository.playIndex(initPosition)
            }
        }
    }

    fun playSong(index: Int? = null){
        if(index != null) playerRepository.playIndex(index)
        else playerRepository.playIndex(currentIndex.value)
    }

    fun playNext(){
        playerRepository.playNext()
    }

    fun playPrevious(){
        playerRepository.playPrevious()
    }

    fun toggleShuffle(){
        isShuffleValue = !isShuffleValue
        if(isShuffleValue){
            _shuffleList.value = _shuffleList.value.shuffled()
        }
        else{
            _shuffleList.value = _shuffleList.value.toMutableList()
        }

        playSong()
    }

    fun toggleRepeat(){
        isRepeatValue = !isRepeatValue

    }

    fun userStartTouch(){
        isUserSeeking = true
    }

    fun userStopTouch(){
        isUserSeeking = false
    }

    fun togglePlayPause() = playerRepository.togglePlayPause()


    fun seekTo(progress: Long) = playerRepository.seekTo(progress)


    override fun onCleared() {
        super.onCleared()
//        player.release()
    }
}