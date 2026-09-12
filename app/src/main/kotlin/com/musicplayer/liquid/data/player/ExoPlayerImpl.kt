package com.musicplayer.liquid.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.musicplayer.liquid.data.model.PlaybackState
import com.musicplayer.liquid.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressUpdateJob: Job? = null
    
    // 播放完成回调
    var onPlaybackCompleted: (() -> Unit)? = null
    
    init {
        // 监听播放器状态变化
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
                // 处理播放完成事件
                if (playbackState == Player.STATE_ENDED) {
                    onTrackEnded()
                }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                // 播放错误时重置状态
                _playbackState.value = PlaybackState(
                    isPlaying = false,
                    currentPosition = 0L,
                    duration = 0L
                )
                stopProgressUpdate()
            }
        })
    }
    
    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressUpdateJob = scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(500) // 每500ms更新一次进度，减少CPU占用
            }
        }
    }
    
    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
    
    private fun onTrackEnded() {
        // 播放完成后重置状态
        _playbackState.value = PlaybackState(
            isPlaying = false,
            currentPosition = 0L,
            duration = _playbackState.value.duration
        )
        // 触发播放完成回调
        onPlaybackCompleted?.invoke()
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
        // 检查是否有曲目,避免没准备就播放导致崩溃
        if (_currentTrack.value != null && player.playbackState != Player.STATE_IDLE) {
            player.play()
        }
    }
    
    override fun seekTo(position: Long) {
        player.seekTo(position)
    }
    
    override fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }
    
    override fun release() {
        stopProgressUpdate()
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
