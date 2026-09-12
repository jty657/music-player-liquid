package com.musicplayer.liquid.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.liquid.data.model.Track
import com.musicplayer.liquid.ui.theme.AnimationConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayQueueSheet(
    queue: List<Track>,
    currentTrack: Track?,
    onPlayTrack: (Track) -> Unit,
    onRemoveFromQueue: (Track) -> Unit,
    onClearQueue: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "播放队列 (${queue.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                if (queue.isNotEmpty()) {
                    TextButton(onClick = onClearQueue) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("清空")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 队列列表
            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "播放队列为空",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(
                        items = queue,
                        key = { it.id }
                    ) { track ->
                        QueueTrackItem(
                            track = track,
                            isCurrentTrack = currentTrack?.id == track.id,
                            onPlayClick = { onPlayTrack(track) },
                            onRemoveClick = { onRemoveFromQueue(track) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QueueTrackItem(
    track: Track,
    isCurrentTrack: Boolean,
    onPlayClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTrack) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 当前播放指示器
            if (isCurrentTrack) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            // 播放按钮（带按压反馈）
            val playInteractionSource = remember { MutableInteractionSource() }
            val playPressed by playInteractionSource.collectIsPressedAsState()
            val playScale by animateFloatAsState(
                targetValue = if (playPressed) AnimationConstants.PRESS_SCALE else 1f,
                animationSpec = tween(AnimationConstants.PRESS_DURATION, easing = AnimationConstants.EASE_OUT),
                label = "queue_play_scale"
            )
            
            IconButton(
                onClick = onPlayClick,
                interactionSource = playInteractionSource,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = playScale
                        scaleY = playScale
                    }
            ) {
                Icon(
                    imageVector = if (isCurrentTrack) Icons.Default.PlayArrow else Icons.Default.PlayCircleOutline,
                    contentDescription = "播放",
                    tint = if (isCurrentTrack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 曲目信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentTrack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 移除按钮（带按压反馈）
            val removeInteractionSource = remember { MutableInteractionSource() }
            val removePressed by removeInteractionSource.collectIsPressedAsState()
            val removeScale by animateFloatAsState(
                targetValue = if (removePressed) AnimationConstants.PRESS_SCALE else 1f,
                animationSpec = tween(AnimationConstants.PRESS_DURATION, easing = AnimationConstants.EASE_OUT),
                label = "queue_remove_scale"
            )
            
            IconButton(
                onClick = onRemoveClick,
                interactionSource = removeInteractionSource,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = removeScale
                        scaleY = removeScale
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "移除",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
