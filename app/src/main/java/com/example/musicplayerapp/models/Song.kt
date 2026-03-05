package com.example.musicplayerapp.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String?,
    val artist: String?,
    val data: String?,
    val albumId : Long,
    val thumbnailUrl: String?,
    val contentUri: Uri
) : Parcelable