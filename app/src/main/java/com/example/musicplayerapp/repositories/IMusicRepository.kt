package com.example.musicplayerapp.repositories

import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.utils.Resource

interface IMusicRepository {
    suspend fun getDownloadedMusicFromDevice() : Resource<List<Song>>
}