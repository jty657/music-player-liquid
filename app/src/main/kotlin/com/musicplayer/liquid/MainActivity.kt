package com.musicplayer.liquid

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.liquid.data.model.PlaybackMode
import com.musicplayer.liquid.data.model.Track
import com.musicplayer.liquid.ui.components.*
import com.musicplayer.liquid.ui.player.PlayerViewModel
import com.musicplayer.liquid.ui.theme.MusicPlayerLiquidTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主Activity
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: PlayerViewModel by viewModels()
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadTracks()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request permission
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permission)
        
        setContent {
            MusicPlayerLiquidTheme(darkTheme = true) {
                MusicPlayerApp(viewModel)
            }
        }
    }
}

@Composable
fun MusicPlayerApp(viewModel: PlayerViewModel) {
    val tracks by viewModel.tracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示错误提示
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 动态背景
            DynamicBackground(albumArtUri = currentTrack?.albumArtUri)
        
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // 标题
            Text(
                text = "🎵 Liquid Music",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
                // 当前播放（带动画）
                AnimatedVisibility(
                    visible = currentTrack != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    currentTrack?.let { track ->
                        Column {
                            PlayerSection(
                                track = track,
                                playbackState = playbackState,
                                playbackMode = playbackMode,
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onPreviousClick = { viewModel.playPrevious() },
                                onNextClick = { viewModel.playNext() },
                                onSeek = { viewModel.seekTo(it) },
                                onModeClick = { viewModel.togglePlaybackMode() }
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            
                // 曲目列表或空状态
                if (tracks.isEmpty()) {
                    EmptyState(message = "未找到音乐文件\n请确保设备中有音乐并授予权限")
                } else {
                    Text(
                        text = "曲目列表 (${tracks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = tracks,
                            key = { it.id } // 优化重组性能
                        ) { track ->
                            TrackItem(
                                track = track,
                                isPlaying = currentTrack?.id == track.id && playbackState.isPlaying,
                                onClick = { viewModel.playTrack(track) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerSection(
    track: Track,
    playbackState: com.musicplayer.liquid.data.model.PlaybackState,
    playbackMode: PlaybackMode,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onModeClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 封面
            AlbumCover(
                albumArtUri = track.albumArtUri,
                isPlaying = playbackState.isPlaying
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 曲目信息
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 进度条
            ProgressBar(
                currentPosition = playbackState.currentPosition,
                duration = playbackState.duration,
                onSeek = onSeek,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 播放模式
                IconButton(onClick = onModeClick) {
                    Icon(
                        imageVector = when (playbackMode) {
                            PlaybackMode.SEQUENTIAL -> Icons.Default.PlayArrow
                            PlaybackMode.REPEAT_ALL -> Icons.Default.Repeat
                            PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne
                            PlaybackMode.SHUFFLE -> Icons.Default.Shuffle
                        },
                        contentDescription = "播放模式",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // 上一曲
                IconButton(onClick = onPreviousClick) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "上一曲",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 播放/暂停
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                // 下一曲
                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "下一曲",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 占位（保持对称）
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun TrackItem(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick // 使用GlassCard内建的波纹效果
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 专辑封面缩略图（带fallback）
            coil.compose.SubcomposeAsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 曲目信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 播放指示器
            if (isPlaying) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "正在播放",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
