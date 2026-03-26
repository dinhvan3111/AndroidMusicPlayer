package com.example.musicplayerapp.ui.viewmodels

import android.media.MediaScannerConnection
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.repositories.IMusicRepository
import com.example.musicplayerapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class MusicLibraryViewModel(
    val musicRepository : IMusicRepository
) : ViewModel() {
    private val _downloadedMusicList : MutableStateFlow<Resource<List<Song>>> =
        MutableStateFlow(Resource.Loading())
    val downloadedMusicList = _downloadedMusicList.asStateFlow()
    private val _isLoadingData = MutableStateFlow(false)
    val isLoadingData = _isLoadingData.asStateFlow()

    fun getListDownloadedMusic() = viewModelScope.launch {
        _isLoadingData.value = true
        try {
            _downloadedMusicList.value = Resource.Loading()
            musicRepository.scanMediaFiles()
            val response = musicRepository.getDownloadedMusicFromDevice()
            if(response is Resource.Success){
                _downloadedMusicList.value = response
            }
            else{
                _downloadedMusicList.value = Resource.Error(message = "Somthing went wrong")
            }
        }
        catch (e: Exception){

        }
        finally {
            _isLoadingData.value = false
        }
    }
}