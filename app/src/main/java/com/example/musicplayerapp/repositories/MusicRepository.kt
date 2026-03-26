package com.example.musicplayerapp.repositories

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

class MusicRepository(
    private val context: Context
) : IMusicRepository {
    override suspend fun getDownloadedMusicFromDevice(): Resource<List<Song>> = withContext(Dispatchers.IO){
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
        )
//        val selection = "${MediaStore.Audio.Media.IS_MUSIC} !=0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                uri,
                projection,
//                selection,
                null,
                null,
                sortOrder,
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIDCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while(cursor.moveToNext()){
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(uri,id)

                    songs.add(
                        Song(
                            id = id,
                            title = cursor.getString(titleCol),
                            artist = cursor.getString(artistCol),
                            data = cursor.getString(dataCol),
                            albumId = cursor.getLong(albumIDCol),
                            thumbnailUrl = "",
                            contentUri = contentUri
                        )
                    )
                }
            }
        }
        catch (ex: Exception){
            return@withContext Resource.Error(message = ex.message ?: "Unknown error")
        }

        Resource.Success(songs)
    }

    private fun getMusicDir(): File? {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    override suspend fun scanMediaFiles() = suspendCancellableCoroutine<Unit> { cont ->
        val musicDir = getMusicDir()
        val files = musicDir?.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in listOf("mp3", "wav", "m4a") }
            ?.map { it.absolutePath }
            ?.toTypedArray()
        if (files.isNullOrEmpty()) {
            cont.resume(Unit) {}
            return@suspendCancellableCoroutine
        }

        MediaScannerConnection.scanFile(context, files, null) { _, _ ->
            if (!cont.isCompleted) cont.resume(Unit) {}
        }
    }
}