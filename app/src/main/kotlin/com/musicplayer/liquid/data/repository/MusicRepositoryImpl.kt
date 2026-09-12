package com.musicplayer.liquid.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.musicplayer.liquid.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "music_prefs")

/**
 * 音乐库仓库实现
 * 使用MediaStore API扫描设备音乐文件
 */
@Singleton
class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MusicRepository {
    
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    private val favoriteIdsKey = stringSetPreferencesKey("favorite_track_ids")
    private val isDarkThemeKey = booleanPreferencesKey("is_dark_theme")
    
    override fun getAllTracks(): Flow<List<Track>> = _tracks.asStateFlow()
    
    override suspend fun getTrackById(id: Long): Track? {
        return _tracks.value.find { it.id == id }
    }
    
    override suspend fun scanMusicLibrary(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val contentResolver: ContentResolver = context.contentResolver
        
        // 读取收藏列表
        val favoriteIds = context.dataStore.data.first()[favoriteIdsKey] ?: emptySet()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                
                val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
                
                tracks.add(
                    Track(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = contentUri,
                        albumArtUri = albumArtUri,
                        isFavorite = favoriteIds.contains(id.toString())
                    )
                )
            }
        }
        
        _tracks.value = tracks
        tracks
    }
    
    override suspend fun toggleFavorite(trackId: Long) {
        context.dataStore.edit { prefs ->
            val favorites = prefs[favoriteIdsKey]?.toMutableSet() ?: mutableSetOf()
            val trackIdStr = trackId.toString()
            
            if (favorites.contains(trackIdStr)) {
                favorites.remove(trackIdStr)
            } else {
                favorites.add(trackIdStr)
            }
            
            prefs[favoriteIdsKey] = favorites
        }
        
        // 更新当前列表
        val favorites = context.dataStore.data.first()[favoriteIdsKey] ?: emptySet()
        _tracks.value = _tracks.value.map { track ->
            if (track.id == trackId) {
                track.copy(isFavorite = favorites.contains(trackId.toString()))
            } else {
                track
            }
        }
    }
    
    override fun getFavoriteTracks(): Flow<List<Track>> {
        return _tracks.map { tracks ->
            tracks.filter { it.isFavorite }
        }
    }
    
    override fun getThemePreference(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[isDarkThemeKey] ?: true // 默认深色模式
        }
    }
    
    override suspend fun saveThemePreference(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[isDarkThemeKey] = isDark
        }
    }
}
