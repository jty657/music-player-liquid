package com.musicplayer.liquid.data.model

import android.net.Uri

/**
 * 音乐曲目数据模型
 */
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,
    val albumArtUri: Uri?,
    val isFavorite: Boolean = false
)

/**
 * 播放状态
 */
data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L
)
