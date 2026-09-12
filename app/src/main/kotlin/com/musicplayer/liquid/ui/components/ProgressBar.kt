package com.musicplayer.liquid.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicplayer.liquid.util.TimeFormatter

/**
 * 播放进度条组件
 * 优化：拖动时暂时不触发 seekTo，松手后才执行
 */
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
    
    Column(modifier = modifier) {
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
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            ),
            interactionSource = remember { MutableInteractionSource() }
        )
        
        // 时间显示（拖动时显示临时位置）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = TimeFormatter.formatTime(displayPosition.toLong()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = TimeFormatter.formatTime(duration),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
