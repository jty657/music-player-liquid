package com.musicplayer.liquid.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.liquid.data.model.PlaybackState
import com.musicplayer.liquid.data.model.Track
import com.musicplayer.liquid.data.player.MusicPlayer
import com.musicplayer.liquid.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    
    fun loadTracks() {
        viewModelScope.launch {
            try {
                musicRepository.scanMusicLibrary()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun playTrack(track: Track) {
        viewModelScope.launch {
            musicPlayer.play(track)
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
