package com.example.musicplayerapp.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.repositories.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (private val playerRepository: PlayerRepository)
    : ViewModel() {

    val songList = playerRepository.songList
    val currentIndex = playerRepository.currentIndex
    val isPlaying = playerRepository.isPlaying


    val currentSong : StateFlow<Song?> =
        combine(songList, currentIndex) { songs, index ->
            songs.getOrNull(index)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )

    fun togglePlayPause() {
        playerRepository.togglePlayPause()
    }

    fun playNext(){
        playerRepository.playNext()
    }
}