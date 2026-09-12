package com.musicplayer.liquid.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.musicplayer.liquid.data.model.PlaybackState
import com.musicplayer.liquid.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ExoPlayer播放器实现
 */
@Singleton
class ExoPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MusicPlayer {
    
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    override val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    
    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    init {
        // 监听播放器状态变化
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }
        })
    }
    
    override suspend fun play(track: Track) {
        _currentTrack.value = track
        val mediaItem = MediaItem.fromUri(track.uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
    
    override fun pause() {
        player.pause()
    }
    
    override fun resume() {
        player.play()
    }
    
    override fun seekTo(position: Long) {
        player.seekTo(position)
    }
    
    override fun release() {
        player.release()
    }
    
    private fun updatePlaybackState() {
        _playbackState.value = PlaybackState(
            isPlaying = player.isPlaying,
            currentPosition = player.currentPosition,
            duration = player.duration.coerceAtLeast(0L)
        )
    }
}
