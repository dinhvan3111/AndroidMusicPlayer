package com.example.musicplayerapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.repositories.IMusicRepository
import com.example.musicplayerapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicLibraryViewModel(
    val musicRepository : IMusicRepository
) : ViewModel() {
    private val _downloadedMusicList : MutableStateFlow<Resource<List<Song>>> =
        MutableStateFlow(Resource.Loading())
    val downloadedMusicList = _downloadedMusicList.asStateFlow()
    init {
//        getListDownloadedMusic()
    }

    fun getListDownloadedMusic() = viewModelScope.launch {
        try {
            _downloadedMusicList.value = Resource.Loading()
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
    }

//    private fun hanldeMusicLibraryResponse(response: Response<Song>) : Resource<NewsResponse>{
//        if(response.isSuccessful){
//            response.body()?.let { resultResponse ->
//                return Resource.Success(resultResponse)
//            }
//        }
//        return Resource.Error(response.message())
//    }
}