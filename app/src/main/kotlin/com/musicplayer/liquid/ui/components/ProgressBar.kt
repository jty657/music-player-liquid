package com.musicplayer.liquid.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.musicplayer.liquid.util.TimeFormatter
import com.musicplayer.liquid.ui.theme.LiquidCyan
import com.musicplayer.liquid.ui.theme.LiquidPink
import com.musicplayer.liquid.ui.theme.AnimationConstants

/**
 * 播放进度条组件 - v2.0 液态玻璃增强版
 * 
 * 特性：
 * - 高亮渐变进度条（Cyan → Pink）
 * - 可拖动seek
 * - 实时时间显示
 * - 液态玻璃容器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var tempPosition by remember(currentPosition) { mutableStateOf(currentPosition.toFloat()) }
    
    // 拖动时使用临时值，否则使用实际值
    val displayPosition = if (isDragging) tempPosition else currentPosition.toFloat()
    
    // 拖动时放大thumb，增强视觉反馈
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.3f else 1f,
        animationSpec = tween(AnimationConstants.ENTRANCE_DURATION, easing = AnimationConstants.EASE_OUT),
        label = "thumb_scale"
    )
    
    GlassCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 进度条
            Slider(
                value = if (duration > 0) displayPosition else 0f,
                onValueChange = {
                    isDragging = true
                    tempPosition = it
                },
                onValueChangeFinished = {
                    isDragging = false
                    onSeek(tempPosition.toLong())
                },
                valueRange = 0f..duration.coerceAtLeast(1).toFloat(),
                modifier = Modifier.graphicsLayer {
                    // thumb放大时保持轨道不变（只缩放thumb）
                    scaleY = 1f
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = LiquidPink,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                interactionSource = remember { MutableInteractionSource() },
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        modifier = Modifier.graphicsLayer {
                            scaleX = thumbScale
                            scaleY = thumbScale
                        }
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 时间显示（拖动时显示临时位置）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = TimeFormatter.formatTime(displayPosition.toLong()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = TimeFormatter.formatTime(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
