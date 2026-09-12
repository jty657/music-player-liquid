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
    
    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(trackId: Long)
    
    /**
     * 获取收藏曲目
     */
    fun getFavoriteTracks(): Flow<List<Track>>
    
    /**
     * 获取深色模式偏好
     */
    fun getThemePreference(): Flow<Boolean>
    
    /**
     * 保存深色模式偏好
     */
    suspend fun saveThemePreference(isDark: Boolean)
}
