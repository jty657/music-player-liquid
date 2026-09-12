package com.musicplayer.liquid.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.liquid.data.model.PlaybackMode
import com.musicplayer.liquid.data.model.PlaybackState
import com.musicplayer.liquid.data.model.Track
import com.musicplayer.liquid.data.player.MusicPlayer
import com.musicplayer.liquid.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var currentIndex = -1
    
    fun loadTracks() {
        viewModelScope.launch {
            try {
                musicRepository.scanMusicLibrary()
            } catch (e: Exception) {
                _errorMessage.value = "加载音乐失败: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun playTrack(track: Track) {
        viewModelScope.launch {
            try {
                currentIndex = tracks.value.indexOf(track)
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
            PlaybackMode.SHUFFLE -> Random.nextInt(trackList.size)
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
            PlaybackMode.SHUFFLE -> Random.nextInt(trackList.size)
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
    
    override fun onCleared() {
        super.onCleared()
        musicPlayer.release()
    }
}
