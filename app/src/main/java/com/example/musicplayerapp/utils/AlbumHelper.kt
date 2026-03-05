package com.example.musicplayerapp.utils

import android.content.Context
import android.net.Uri

class AlbumHelper {
    companion object{
        fun hasAlbumArt(context: Context, albumArtUri: Uri): Boolean {
            return try {
                context.contentResolver.openInputStream(albumArtUri)?.use {
                    true
                } ?: false
            } catch (e: Exception) {
                false
            }
        }
    }
}