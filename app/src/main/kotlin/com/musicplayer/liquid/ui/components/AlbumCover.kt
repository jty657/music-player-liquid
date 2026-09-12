package com.musicplayer.liquid.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.musicplayer.liquid.ui.theme.LiquidCyan
import com.musicplayer.liquid.ui.theme.LiquidPink

/**
 * 专辑封面显示组件
 * 带旋转动画、光晕效果和加载占位符
 */
@Composable
fun AlbumCover(
    albumArtUri: Any?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    // 只在isPlaying=true时运行旋转动画
    val rotation = if (isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
        val animatedRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "album_rotation"
        )
        animatedRotation
    } else {
        0f
    }
    
    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // 光晕效果
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LiquidCyan.copy(alpha = 0.3f),
                                LiquidPink.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // 专辑封面（带加载fallback）
        SubcomposeAsyncImage(
            model = albumArtUri,
            contentDescription = "专辑封面",
            contentScale = ContentScale.Crop,
            loading = {
                // 加载中显示占位符 + 脉冲动画
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            },
            error = {
                // 加载失败显示占位符
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            },
            modifier = Modifier
                .size(280.dp)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .rotate(rotation)
        )
    }
}
