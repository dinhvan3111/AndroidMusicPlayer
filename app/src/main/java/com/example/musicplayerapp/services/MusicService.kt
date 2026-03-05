package com.example.musicplayerapp.services

import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken

class MusicService : MediaSessionService(){
    private var controller: MediaController? = null
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo?): MediaSession? {
        TODO("Not yet implemented")
    }

    fun connectToService(){
        val sessionToken = SessionToken(
            application,
            ComponentName(application, MusicService::class.java)
        )

        val controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()

        controllerFuture.addListener({
            controller = controllerFuture.get()
        }, ContextCompat.getMainExecutor(application))
    }

    fun play(uri: Uri){
        val mediaItem = MediaItem.fromUri(uri)
        controller?.setMediaItem(mediaItem)
        controller?.prepare()
        controller?.play()
    }

    fun pause(){
        controller?.pause()
    }
}