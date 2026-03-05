package com.example.musicplayerapp.ui.viewmodels

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.widget.MediaController
import androidx.lifecycle.ViewModel
import androidx.media3.session.SessionToken
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.services.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(
    private val initSongList : List<Song>,
    private val initPosition: Int
) : ViewModel() {
    private var _songList: MutableStateFlow<List<Song>> = MutableStateFlow(initSongList)
    val songList: StateFlow<List<Song>> = _songList

    private var _shuffleList : MutableStateFlow<List<Song>> = MutableStateFlow(listOf<Song>())
    val shuffleList: StateFlow<List<Song>> = _shuffleList

    private var _currentIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentIndexFlow: StateFlow<Int> = _currentIndex
    var currentIndexValue : Int
        get() = _currentIndex.value
        set(value) { _currentIndex.value = value}



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

    init {
        _shuffleList.value = ArrayList<Song>(_songList.value)
        currentIndexValue = initPosition
    }

    fun play(uri: Uri){

    }

    fun playNext(){
        currentIndexValue = (currentIndexValue + 1) % (if(_isShuffle.value) _shuffleList.value.size else _songList.value.size)
    }

    fun playPrevious(){
        val listSize = if(isShuffleValue) _shuffleList.value.size else _songList.value.size
        currentIndexValue = (currentIndexValue - 1 + listSize) % listSize
    }

    fun toggleShuffle(){
        isShuffleValue = !isShuffleValue
        if(isShuffleValue){
            _shuffleList.value = _shuffleList.value.shuffled()
        }
        else{
            _shuffleList.value = _shuffleList.value.toMutableList()
        }
    }
    fun toggleRepeat(){
        isRepeatValue = !isRepeatValue

    }
}