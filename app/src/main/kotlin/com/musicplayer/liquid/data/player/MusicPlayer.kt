package com.musicplayer.liquid.data.player

import com.musicplayer.liquid.data.model.PlaybackState
import com.musicplayer.liquid.data.model.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * 音乐播放器接口
 */
interface MusicPlayer {
    /**
     * 当前播放曲目
     */
    val currentTrack: StateFlow<Track?>
    
    /**
     * 播放状态
     */
    val playbackState: StateFlow<PlaybackState>
    
    /**
     * 播放指定曲目
     */
    suspend fun play(track: Track)
    
    /**
     * 暂停播放
     */
    fun pause()
    
    /**
     * 恢复播放
     */
    fun resume()
    
    /**
     * 跳转到指定位置
     */
    fun seekTo(position: Long)
    
    /**
     * 释放资源
     */
    fun release()
}
