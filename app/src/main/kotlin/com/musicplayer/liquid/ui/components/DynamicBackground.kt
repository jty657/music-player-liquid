package com.musicplayer.liquid.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 动态液态背景组件 - v3.0 性能优化版
 * 
 * 核心特性：
 * - 从专辑封面提取主色调（Palette缓存）
 * - 液态波动效果（双层异相波动）
 * - 平滑颜色过渡（1200ms缓动）
 * 
 * 性能优化：
 * - 移除纯装饰的粒子层
 * - 波动周期：10s主层 + 13s副层（素数周期避免视觉重复）
 */
@Composable
fun DynamicBackground(
    albumArtUri: Uri?,
    modifier: Modifier = Modifier
) {
    var dominantColor by remember { mutableStateOf(Color(0xFF1A1A2E)) }
    var vibrantColor by remember { mutableStateOf(Color(0xFF16213E)) }
    
    val animatedDominant by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "dominant"
    )
    
    val animatedVibrant by animateColorAsState(
        targetValue = vibrantColor,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "vibrant"
    )
    
    // 双层波动动画
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_wave")
    val wave1Phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    
    val wave2Phase by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    
    val context = LocalContext.current
    
    LaunchedEffect(albumArtUri) {
        if (albumArtUri != null) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(albumArtUri)
                        .allowHardware(false)
                        .build()
                    
                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        (result.drawable as? BitmapDrawable)?.bitmap
                    } else null
                }
                
                bitmap?.let {
                    val cacheKey = albumArtUri.toString()
                    val palette = withContext(Dispatchers.Default) {
                        com.musicplayer.liquid.util.PaletteCache.getOrExtract(cacheKey, it)
                    }
                    
                    palette.vibrantSwatch?.rgb?.let { rgb ->
                        vibrantColor = Color(rgb).copy(alpha = 0.7f)
                    }
                    palette.darkVibrantSwatch?.rgb?.let { rgb ->
                        dominantColor = Color(rgb).copy(alpha = 0.9f)
                    }
                }
            } catch (e: Exception) {
                // Use default colors on error
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .drawBehind {
                // 主渐变背景
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            animatedDominant,
                            animatedVibrant,
                            Color(0xFF0F0F0F)
                        )
                    )
                )
                
                // 液态波动层1（主层）
                val angle1 = Math.toRadians(wave1Phase.toDouble()).toFloat()
                val offsetX1 = size.width * 0.2f * cos(angle1)
                val offsetY1 = size.height * 0.15f * sin(angle1 * 0.7f)
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedVibrant.copy(alpha = 0.4f),
                            animatedVibrant.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = Offset(
                            size.width * 0.7f + offsetX1,
                            size.height * 0.3f + offsetY1
                        ),
                        radius = size.minDimension * 0.8f
                    ),
                    radius = size.minDimension * 0.8f,
                    center = Offset(
                        size.width * 0.7f + offsetX1,
                        size.height * 0.3f + offsetY1
                    )
                )
                
                // 液态波动层2（副层，相位相反，周期不同）
                val angle2 = Math.toRadians(wave2Phase.toDouble()).toFloat()
                val offsetX2 = size.width * 0.15f * cos(angle2 * 1.3f)
                val offsetY2 = size.height * 0.2f * sin(angle2)
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedDominant.copy(alpha = 0.35f),
                            animatedDominant.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(
                            size.width * 0.3f + offsetX2,
                            size.height * 0.6f + offsetY2
                        ),
                        radius = size.minDimension * 0.7f
                    ),
                    radius = size.minDimension * 0.7f,
                    center = Offset(
                        size.width * 0.3f + offsetX2,
                        size.height * 0.6f + offsetY2
                    )
                )
            }
    )
}
