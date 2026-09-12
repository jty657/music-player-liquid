package com.musicplayer.liquid.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.liquid.data.model.PlaybackMode
import com.musicplayer.liquid.data.model.PlaybackState
import com.musicplayer.liquid.data.model.Track
import com.musicplayer.liquid.data.model.SortOption
import com.musicplayer.liquid.data.model.SleepTimer
import com.musicplayer.liquid.data.player.MusicPlayer
import com.musicplayer.liquid.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * 播放器ViewModel
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer
) : ViewModel() {
    
    val tracks: StateFlow<List<Track>> = musicRepository.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val currentTrack: StateFlow<Track?> = musicPlayer.currentTrack
    
    val playbackState: StateFlow<PlaybackState> = musicPlayer.playbackState
    
    private val _playbackMode = MutableStateFlow(PlaybackMode.SEQUENTIAL)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()
    
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()
    
    private val _sortOption = MutableStateFlow(SortOption.TITLE_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    private val _sleepTimer = MutableStateFlow(SleepTimer())
    val sleepTimer: StateFlow<SleepTimer> = _sleepTimer.asStateFlow()
    
    private var sleepTimerJob: Job? = null
    
    // 播放队列
    private val _playQueue = MutableStateFlow<List<Track>>(emptyList())
    val playQueue: StateFlow<List<Track>> = _playQueue.asStateFlow()
    
    // 播放历史（最近播放）
    private val _recentlyPlayed = MutableStateFlow<List<Track>>(emptyList())
    val recentlyPlayed: StateFlow<List<Track>> = _recentlyPlayed.asStateFlow()
    
    val filteredTracks: StateFlow<List<Track>> = combine(
        tracks,
        _searchQuery,
        _showOnlyFavorites,
        _sortOption
    ) { tracks, query, onlyFavorites, sortOption ->
        var result = tracks
        
        // 先过滤收藏
        if (onlyFavorites) {
            result = result.filter { it.isFavorite }
        }
        
        // 再搜索过滤
        if (query.isNotBlank()) {
            result = result.filter { track ->
                track.title.contains(query, ignoreCase = true) ||
                track.artist.contains(query, ignoreCase = true) ||
                track.album.contains(query, ignoreCase = true)
            }
        }
        
        // 排序
        result = when (sortOption) {
            SortOption.TITLE_ASC -> result.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> result.sortedByDescending { it.title.lowercase() }
            SortOption.ARTIST_ASC -> result.sortedBy { it.artist.lowercase() }
            SortOption.ARTIST_DESC -> result.sortedByDescending { it.artist.lowercase() }
            SortOption.DURATION_ASC -> result.sortedBy { it.duration }
            SortOption.DURATION_DESC -> result.sortedByDescending { it.duration }
            SortOption.DATE_ADDED -> result // 保持原顺序(按扫描顺序)
        }
        
        result
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private var currentIndex = -1
    private val playHistory = mutableListOf<Int>() // Shuffle 模式播放历史
    private var shuffledIndices = listOf<Int>() // 随机播放索引列表
    
    init {
        // 设置播放完成回调
        if (musicPlayer is com.musicplayer.liquid.data.player.ExoPlayerImpl) {
            musicPlayer.onPlaybackCompleted = {
                handlePlaybackCompleted()
            }
        }
        
        // 初始化随机播放列表
        viewModelScope.launch {
            tracks.collect { trackList ->
                if (trackList.isNotEmpty()) {
                    shuffledIndices = trackList.indices.shuffled()
                }
            }
        }
    }
    
    fun loadTracks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                musicRepository.scanMusicLibrary()
            } catch (e: Exception) {
                _errorMessage.value = "加载音乐失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun playTrack(track: Track) {
        viewModelScope.launch {
            try {
                val trackList = tracks.value
                currentIndex = trackList.indexOf(track)
                
                // Shuffle 模式下更新历史
                if (_playbackMode.value == PlaybackMode.SHUFFLE && currentIndex >= 0) {
                    playHistory.add(currentIndex)
                }
                
                // 添加到播放历史（最近20首）
                val recentList = _recentlyPlayed.value.toMutableList()
                recentList.removeAll { it.id == track.id } // 移除旧记录
                recentList.add(0, track) // 添加到开头
                _recentlyPlayed.value = recentList.take(20)
                
                musicPlayer.play(track)
            } catch (e: Exception) {
                _errorMessage.value = "播放失败: ${e.message}"
            }
        }
    }
    
    fun playNext() {
        val trackList = tracks.value
        if (trackList.isEmpty()) return
        
        currentIndex = when (_playbackMode.value) {
            PlaybackMode.SHUFFLE -> {
                // 使用预生成的随机列表
                val currentShufflePos = shuffledIndices.indexOf(currentIndex)
                val nextShufflePos = (currentShufflePos + 1) % shuffledIndices.size
                val nextIndex = shuffledIndices[nextShufflePos]
                playHistory.add(nextIndex)
                nextIndex
            }
            PlaybackMode.REPEAT_ONE -> currentIndex
            else -> {
                val next = currentIndex + 1
                if (next >= trackList.size) {
                    if (_playbackMode.value == PlaybackMode.REPEAT_ALL) 0 else return
                } else next
            }
        }
        
        playTrack(trackList[currentIndex])
    }
    
    fun playPrevious() {
        val trackList = tracks.value
        if (trackList.isEmpty()) return
        
        currentIndex = when (_playbackMode.value) {
            PlaybackMode.SHUFFLE -> {
                // 从播放历史中回退
                if (playHistory.size > 1) {
                    playHistory.removeAt(playHistory.lastIndex) // 移除当前
                    playHistory.last() // 返回上一首
                } else {
                    // 无历史则使用随机列表前一首
                    val currentShufflePos = shuffledIndices.indexOf(currentIndex)
                    val prevShufflePos = if (currentShufflePos > 0) currentShufflePos - 1 else shuffledIndices.lastIndex
                    shuffledIndices[prevShufflePos]
                }
            }
            PlaybackMode.REPEAT_ONE -> currentIndex
            else -> {
                val prev = currentIndex - 1
                if (prev < 0) {
                    if (_playbackMode.value == PlaybackMode.REPEAT_ALL) trackList.size - 1 else return
                } else prev
            }
        }
        
        playTrack(trackList[currentIndex])
    }
    
    fun togglePlaybackMode() {
        _playbackMode.value = when (_playbackMode.value) {
            PlaybackMode.SEQUENTIAL -> PlaybackMode.REPEAT_ALL
            PlaybackMode.REPEAT_ALL -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.SEQUENTIAL
        }
        
        // 切换到 Shuffle 时重新洗牌并清空历史
        if (_playbackMode.value == PlaybackMode.SHUFFLE) {
            shuffledIndices = tracks.value.indices.shuffled()
            playHistory.clear()
            if (currentIndex >= 0) {
                playHistory.add(currentIndex)
            }
        }
    }
    
    private fun handlePlaybackCompleted() {
        // 根据播放模式自动处理
        when (_playbackMode.value) {
            PlaybackMode.REPEAT_ONE -> {
                // 单曲循环 - 重新播放当前曲目
                currentTrack.value?.let { playTrack(it) }
            }
            PlaybackMode.SEQUENTIAL, PlaybackMode.REPEAT_ALL, PlaybackMode.SHUFFLE -> {
                // 其他模式 - 播放下一曲
                playNext()
            }
        }
    }
    
    fun togglePlayPause() {
        if (playbackState.value.isPlaying) {
            musicPlayer.pause()
        } else {
            musicPlayer.resume()
        }
    }
    
    fun seekTo(position: Long) {
        musicPlayer.seekTo(position)
    }
    
    fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
        musicPlayer.setVolume(_volume.value)
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun toggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }
    
    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch {
            try {
                musicRepository.toggleFavorite(trackId)
            } catch (e: Exception) {
                _errorMessage.value = "操作失败: ${e.message}"
            }
        }
    }
    
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }
    
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
    
    fun startSleepTimer(durationMillis: Long) {
        sleepTimerJob?.cancel()
        
        _sleepTimer.value = SleepTimer(
            enabled = true,
            remainingMillis = durationMillis,
            totalMillis = durationMillis
        )
        
        sleepTimerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationMillis
            
            while (isActive && System.currentTimeMillis() < endTime) {
                val remaining = endTime - System.currentTimeMillis()
                _sleepTimer.value = _sleepTimer.value.copy(
                    remainingMillis = remaining.coerceAtLeast(0L)
                )
                delay(1000) // 每秒更新
            }
            
            // 定时器到点，暂停播放
            if (isActive) {
                pause()
                _sleepTimer.value = SleepTimer()
            }
        }
    }
    
    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimer.value = SleepTimer()
    }
    
    fun addToQueue(track: Track) {
        val current = _playQueue.value.toMutableList()
        if (!current.contains(track)) {
            current.add(track)
            _playQueue.value = current
        }
    }
    
    fun removeFromQueue(track: Track) {
        _playQueue.value = _playQueue.value.filter { it.id != track.id }
    }
    
    fun clearQueue() {
        _playQueue.value = emptyList()
    }
    
    override fun onCleared() {
        super.onCleared()
        // 清理定时器
        sleepTimerJob?.cancel()
        // 清理回调避免内存泄漏
        if (musicPlayer is com.musicplayer.liquid.data.player.ExoPlayerImpl) {
            musicPlayer.onPlaybackCompleted = null
        }
        musicPlayer.release()
    }
}
