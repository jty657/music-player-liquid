package com.musicplayer.liquid.data.repository

import com.musicplayer.liquid.data.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * 音乐库仓库接口
 */
interface MusicRepository {
    /**
     * 获取所有音乐曲目
     */
    fun getAllTracks(): Flow<List<Track>>
    
    /**
     * 根据ID获取曲目
     */
    suspend fun getTrackById(id: Long): Track?
    
    /**
     * 扫描设备音乐库
     */
    suspend fun scanMusicLibrary(): List<Track>
}
