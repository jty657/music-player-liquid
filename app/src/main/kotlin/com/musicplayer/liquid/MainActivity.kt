package com.musicplayer.liquid

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
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
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            
            MusicPlayerLiquidTheme(darkTheme = isDarkTheme) {
                MusicPlayerApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp(viewModel: PlayerViewModel) {
    val tracks by viewModel.tracks.collectAsState()
    val filteredTracks by viewModel.filteredTracks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sleepTimer by viewModel.sleepTimer.collectAsState()
    val playQueue by viewModel.playQueue.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    
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
            
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎵 Liquid Music",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // 顶部工具栏
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 主题切换（深色模式显示太阳图标，表示"切换到亮色"）
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "切换到亮色模式" else "切换到深色模式",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (tracks.isNotEmpty()) {
                        // 睡眠定时器
                        SleepTimerButton(
                            sleepTimer = sleepTimer,
                            onClick = { showSleepTimerDialog = true }
                        )
                        
                        // 播放队列
                        IconButton(onClick = { showQueueSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (playQueue.isNotEmpty()) {
                                        Badge { Text("${playQueue.size}") }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = "播放队列",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // 排序
                        SortMenu(
                            currentSort = sortOption,
                            onSortSelected = { viewModel.setSortOption(it) }
                        )
                        
                        // 收藏过滤
                        IconButton(onClick = { viewModel.toggleShowOnlyFavorites() }) {
                            Icon(
                                imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (showOnlyFavorites) "显示全部" else "只看收藏",
                                tint = if (showOnlyFavorites) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        // 搜索
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }
            
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
                                volume = volume,
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onPreviousClick = { viewModel.playPrevious() },
                                onNextClick = { viewModel.playNext() },
                                onSeek = { viewModel.seekTo(it) },
                                onModeClick = { viewModel.togglePlaybackMode() },
                                onVolumeChange = { viewModel.setVolume(it) },
                                onFavoriteClick = { viewModel.toggleFavorite(track.id) }
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            
                // 加载状态
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 曲目列表或空状态
                if (!isLoading && tracks.isEmpty()) {
                    EmptyState(message = "未找到音乐文件\n请确保设备中有音乐并授予权限")
                } else if (!isLoading && tracks.isNotEmpty()) {
                    val displayTracks = filteredTracks
                    
                    if (displayTracks.isEmpty() && searchQuery.isNotEmpty()) {
                        // 搜索无结果
                        EmptyState(message = "未找到匹配的音乐\n试试其他关键词")
                    } else if (displayTracks.isEmpty() && showOnlyFavorites) {
                        // 收藏为空
                        EmptyState(message = "还没有收藏的音乐\n点击♥收藏你喜欢的音乐")
                    } else {
                        val totalDuration = displayTracks.sumOf { it.duration }
                        val durationText = com.musicplayer.liquid.util.TimeFormatter.formatTimeLong(totalDuration)
                        
                        // 最近播放（横向滚动）
                        if (recentlyPlayed.isNotEmpty()) {
                            Text(
                                text = "🕒 最近播放",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    count = recentlyPlayed.size,
                                    key = { index -> recentlyPlayed[index].id }
                                ) { index ->
                                    val track = recentlyPlayed[index]
                                    RecentTrackCard(
                                        track = track,
                                        isPlaying = currentTrack?.id == track.id && playbackState.isPlaying,
                                        onClick = { viewModel.playTrack(track) }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "曲目列表 (${displayTracks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "总时长: $durationText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = displayTracks,
                            key = { _, track -> track.id }
                        ) { index, track ->
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = tween(
                                        durationMillis = 200,
                                        delayMillis = (index * 30).coerceAtMost(150),
                                        easing = LinearOutSlowInEasing
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 200,
                                        delayMillis = (index * 30).coerceAtMost(150),
                                        easing = LinearOutSlowInEasing
                                    )
                                )
                            ) {
                                TrackItem(
                                    track = track,
                                    isPlaying = currentTrack?.id == track.id && playbackState.isPlaying,
                                    onClick = { viewModel.playTrack(track) },
                                    onFavoriteClick = { viewModel.toggleFavorite(track.id) },
                                    onAddToQueueClick = { viewModel.addToQueue(track) }
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }
    
    // 睡眠定时器对话框
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            sleepTimer = sleepTimer,
            onDismiss = { showSleepTimerDialog = false },
            onStartTimer = { duration ->
                viewModel.startSleepTimer(duration)
            },
            onCancelTimer = { viewModel.cancelSleepTimer() }
        )
    }
    
    // 播放队列Sheet
    if (showQueueSheet) {
        PlayQueueSheet(
            queue = playQueue,
            currentTrack = currentTrack,
            onPlayTrack = { viewModel.playTrack(it) },
            onRemoveFromQueue = { viewModel.removeFromQueue(it) },
            onClearQueue = { viewModel.clearQueue() },
            onDismiss = { showQueueSheet = false }
        )
    }
}

@Composable
fun PlayerSection(
    track: Track,
    playbackState: com.musicplayer.liquid.data.model.PlaybackState,
    playbackMode: PlaybackMode,
    volume: Float,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onModeClick: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onFavoriteClick: () -> Unit
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
            // 封面（点击切换播放/暂停）
            Box(
                modifier = Modifier
                    .clickable(onClick = onPlayPauseClick)
                    .clip(MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                AlbumCover(
                    albumArtUri = track.albumArtUri,
                    isPlaying = playbackState.isPlaying
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 曲目信息
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 收藏按钮
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (track.isFavorite) "取消收藏" else "收藏",
                        tint = if (track.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 进度条
            ProgressBar(
                currentPosition = playbackState.currentPosition,
                duration = playbackState.duration,
                onSeek = onSeek,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 音量控制
            VolumeControl(
                volume = volume,
                onVolumeChange = onVolumeChange,
                modifier = Modifier.align(Alignment.End)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 播放模式（带动画）
                IconButton(onClick = onModeClick) {
                    AnimatedContent(
                        targetState = playbackMode,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f)).togetherWith(
                                fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f)
                            )
                        },
                        label = "mode_icon_animation"
                    ) { mode ->
                        Icon(
                            imageVector = when (mode) {
                                PlaybackMode.SEQUENTIAL -> Icons.Default.ArrowForward
                                PlaybackMode.REPEAT_ALL -> Icons.Default.Repeat
                                PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne
                                PlaybackMode.SHUFFLE -> Icons.Default.Shuffle
                            },
                            contentDescription = when (mode) {
                                PlaybackMode.SEQUENTIAL -> "顺序播放"
                                PlaybackMode.REPEAT_ALL -> "列表循环"
                                PlaybackMode.REPEAT_ONE -> "单曲循环"
                                PlaybackMode.SHUFFLE -> "随机播放"
                            },
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 上一曲
                IconButton(onClick = onPreviousClick) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "上一曲",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 播放/暂停（带scale动画反馈 - 不自动动画，通过pressable modifier实现按压反馈）
                // 使用Interaction Source跟踪按压状态
                val playPauseInteraction = remember { MutableInteractionSource() }
                val isPressed by playPauseInteraction.collectIsPressedAsState()
                
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.97f else 1f,
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearOutSlowInEasing
                    ),
                    label = "play_button_scale"
                )
                
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    interactionSource = playPauseInteraction,
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
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
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToQueueClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 专辑封面缩略图
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " • ${com.musicplayer.liquid.util.TimeFormatter.formatTime(track.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            // 收藏按钮
            IconButton(
                onClick = { onFavoriteClick() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (track.isFavorite) "取消收藏" else "收藏",
                    tint = if (track.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // 更多菜单
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("添加到队列") },
                        onClick = {
                            onAddToQueueClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                        }
                    )
                }
            }
            
            // 播放指示器（静态，避免列表中50个动画同时运行）
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "正在播放",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 最近播放横向卡片（紧凑液态玻璃风格 + 按压反馈）
 */
@Composable
fun RecentTrackCard(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearOutSlowInEasing
        ),
        label = "recent_card_press"
    )
    
    GlassCard(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 专辑封面缩略图
            Box(
                contentAlignment = Alignment.Center
            ) {
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
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(40.dp)
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
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .size(120.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                
                // 播放指示器
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "正在播放",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 曲目信息
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
