package com.example.musicplayerapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayerapp.repositories.IMusicRepository
import com.example.musicplayerapp.ui.viewmodels.MusicLibraryViewModel

class MusicLibraryViewModelProviderFactory(
    val musicRepository: IMusicRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MusicLibraryViewModel(musicRepository) as T
    }
}