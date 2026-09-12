package com.musicplayer.liquid.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.musicplayer.liquid.ui.theme.AnimationConstants

/**
 * 手势控制增强组件
 * 
 * 功能：
 * - 左滑：上一曲（触发阈值：屏幕宽度的30%）
 * - 右滑：下一曲
 * - 带视觉反馈动画
 * - 符合Emil Kowalski的动量手势原则
 * 
 * 用法：
 * ```
 * Box(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .swipeToControl(
 *             onSwipeLeft = { /* 上一曲 */ },
 *             onSwipeRight = { /* 下一曲 */ }
 *         )
 * ) {
 *     // 你的内容
 * }
 * ```
 */
@Composable
fun Modifier.swipeToControl(
    enabled: Boolean = true,
    swipeThreshold: Float = 0.3f, // 30%屏幕宽度触发
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {}
): Modifier {
    if (!enabled) return this
    
    var offsetX by remember { mutableStateOf(0f) }
    var showHint by remember { mutableStateOf(false) }
    var hintDirection by remember { mutableStateOf(SwipeDirection.NONE) }
    val scope = rememberCoroutineScope()
    
    val animatedOffset = remember { Animatable(0f) }
    
    LaunchedEffect(offsetX) {
        animatedOffset.snapTo(offsetX)
    }
    
    return this
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    scope.launch {
                        val screenWidth = size.width.toFloat()
                        val threshold = screenWidth * swipeThreshold
                        
                        when {
                            offsetX < -threshold -> {
                                // 左滑 - 上一曲
                                onSwipeLeft()
                                showHint = true
                                hintDirection = SwipeDirection.LEFT
                            }
                            offsetX > threshold -> {
                                // 右滑 - 下一曲
                                onSwipeRight()
                                showHint = true
                                hintDirection = SwipeDirection.RIGHT
                            }
                        }
                        
                        // 回弹动画
                        animatedOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = AnimationConstants.ENTRANCE_DURATION,
                                easing = AnimationConstants.EASE_OUT
                            )
                        )
                        offsetX = 0f
                        
                        // 延迟隐藏提示
                        kotlinx.coroutines.delay(500)
                        showHint = false
                        hintDirection = SwipeDirection.NONE
                    }
                },
                onDragCancel = {
                    scope.launch {
                        animatedOffset.animateTo(0f)
                        offsetX = 0f
                    }
                },
                onHorizontalDrag = { _, dragAmount ->
                    offsetX += dragAmount
                    // 边界阻尼（超出屏幕宽度后减速）
                    val screenWidth = size.width.toFloat()
                    if (kotlin.math.abs(offsetX) > screenWidth * 0.5f) {
                        offsetX *= 0.95f // 阻尼系数
                    }
                }
            )
        }
        .graphicsLayer {
            translationX = animatedOffset.value
            alpha = 1f - (kotlin.math.abs(animatedOffset.value) / size.width * 0.3f).coerceIn(0f, 0.3f)
        }
}

private enum class SwipeDirection {
    NONE, LEFT, RIGHT
}

/**
 * 滑动提示图标（可选展示）
 */
@Composable
fun SwipeHintOverlay(
    direction: String, // "left" or "right"
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = when (direction) {
            "left" -> Alignment.CenterStart
            "right" -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Icon(
            imageVector = if (direction == "left") Icons.Default.FastRewind else Icons.Default.FastForward,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(32.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    }
}
