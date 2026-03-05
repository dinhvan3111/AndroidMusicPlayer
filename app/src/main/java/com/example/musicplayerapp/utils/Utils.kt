package com.example.musicplayerapp.utils

class Utils {
    companion object{
        fun formatDurationTime(seconds: Int): String {
            return String.format("%01d:%02d", seconds/60, seconds%60)
        }
    }
}