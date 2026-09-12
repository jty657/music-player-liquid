package com.musicplayer.liquid.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.musicplayer.liquid.data.model.SleepTimer
import com.musicplayer.liquid.util.TimeFormatter
import com.musicplayer.liquid.ui.theme.AnimationConstants

@Composable
fun SleepTimerDialog(
    sleepTimer: SleepTimer,
    onDismiss: () -> Unit,
    onStartTimer: (Long) -> Unit,
    onCancelTimer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("睡眠定时器")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (sleepTimer.enabled) {
                    // 显示剩余时间
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "剩余时间",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = TimeFormatter.formatTimeLong(sleepTimer.remainingMillis),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (sleepTimer.remainingMillis.toFloat() / sleepTimer.totalMillis.toFloat()) },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            onCancelTimer()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("取消定时器")
                    }
                } else {
                    // 选择时长
                    Text(
                        text = "播放结束后自动暂停",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TimerOption("5 分钟", SleepTimer.TIMER_5_MIN, onStartTimer, onDismiss)
                    TimerOption("10 分钟", SleepTimer.TIMER_10_MIN, onStartTimer, onDismiss)
                    TimerOption("15 分钟", SleepTimer.TIMER_15_MIN, onStartTimer, onDismiss)
                    TimerOption("30 分钟", SleepTimer.TIMER_30_MIN, onStartTimer, onDismiss)
                    TimerOption("60 分钟", SleepTimer.TIMER_60_MIN, onStartTimer, onDismiss)
                }
            }
        },
        confirmButton = {
            if (!sleepTimer.enabled) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}

@Composable
private fun TimerOption(
    label: String,
    durationMillis: Long,
    onStartTimer: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) AnimationConstants.PRESS_SCALE else 1f,
        animationSpec = tween(AnimationConstants.PRESS_DURATION, easing = AnimationConstants.EASE_OUT),
        label = "timer_option_scale"
    )
    
    OutlinedButton(
        onClick = {
            onStartTimer(durationMillis)
            onDismiss()
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Text(label)
    }
}
