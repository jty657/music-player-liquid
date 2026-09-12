package com.musicplayer.liquid.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.musicplayer.liquid.data.model.SleepTimer
import com.musicplayer.liquid.util.TimeFormatter

/**
 * 睡眠定时器按钮（带倒计时badge）
 */
@Composable
fun SleepTimerButton(
    sleepTimer: SleepTimer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                if (sleepTimer.enabled && sleepTimer.remainingMillis > 0) {
                    Badge {
                        val minutes = (sleepTimer.remainingMillis / 60000).toInt()
                        Text("${minutes}m")
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = "睡眠定时器",
                tint = if (sleepTimer.enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                }
            )
        }
    }
}
