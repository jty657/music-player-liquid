package com.musicplayer.liquid.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

/**
 * 动态背景组件
 * 根据专辑封面提取主色调并创建渐变背景
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
        animationSpec = tween(800),
        label = "dominant"
    )
    
    val animatedVibrant by animateColorAsState(
        targetValue = vibrantColor,
        animationSpec = tween(800),
        label = "vibrant"
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
                    val palette = withContext(Dispatchers.Default) {
                        Palette.from(it).generate()
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedDominant,
                        animatedVibrant,
                        Color(0xFF0F0F0F)
                    )
                )
            )
    )
}
