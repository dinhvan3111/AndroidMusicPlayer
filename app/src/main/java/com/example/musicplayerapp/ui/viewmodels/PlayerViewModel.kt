package com.example.musicplayerapp.ui.viewmodels

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.models.TimerOption
import com.example.musicplayerapp.repositories.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
sealed class TimerEvent {
    data class Started(val minutes: Int) : TimerEvent()
    object Cancelled : TimerEvent()
    object Finished : TimerEvent()
}
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

    private val _selectedTimer = MutableStateFlow<TimerOption?>(null)
    val selectedTimer = _selectedTimer.asStateFlow()

    private val _remainingTime = MutableStateFlow<Long?>(null)
    val remainingTime = _remainingTime.asStateFlow()

    val currentSong : StateFlow<Song?> =
        combine(songList, currentIndex) { songs, index ->
            songs.getOrNull(index)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )

    private var countDownTimer: CountDownTimer? = null
    private val _timerEvent = MutableSharedFlow<TimerEvent?>(replay = 0, extraBufferCapacity = 1)
    val timerEvent = _timerEvent.asSharedFlow()

    val timerOptions = listOf(
        TimerOption("1 minutes", 1),
        TimerOption("5 minutes", 5),
        TimerOption("15 minutes", 15),
        TimerOption("30 minutes", 30),
        TimerOption("1 hour", 60),
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

    fun startTimer(timer: TimerOption){
        stopTimer()
        val timerDuration = timer.minutes * 60 * 1000L
        _selectedTimer.value = timer
        viewModelScope.launch {
            _timerEvent.emit(TimerEvent.Started(timer.minutes))
        }
        countDownTimer = object: CountDownTimer(timerDuration, 1000){
            override fun onFinish() {
                togglePlayPause()
                countDownTimer = null
                _selectedTimer.value = null
                _remainingTime.value = null
                viewModelScope.launch {
                    _timerEvent.emit(TimerEvent.Finished)
                }
            }
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
            }
        }.start()

    }

    fun stopTimer(){
        if(countDownTimer != null){
            _selectedTimer.value = null
            countDownTimer?.cancel()
            viewModelScope.launch {
                _timerEvent.emit(TimerEvent.Cancelled)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
//        player.release()
    }
}