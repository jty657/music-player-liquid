package com.musicplayer.liquid.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.musicplayer.liquid.ui.theme.AnimationConstants

/**
 * 通用按压反馈Modifier扩展
 * 符合Emil Kowalski设计原则：按钮必须有响应式反馈
 * 
 * 用法：
 * ```
 * IconButton(
 *     onClick = { },
 *     modifier = Modifier.pressableFeedback(interactionSource)
 * ) { Icon(...) }
 * ```
 */
@Composable
fun Modifier.pressableFeedback(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = AnimationConstants.PRESS_SCALE,
    duration: Int = AnimationConstants.PRESS_DURATION
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(
            durationMillis = duration,
            easing = AnimationConstants.EASE_OUT
        ),
        label = "press_scale"
    )
    
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
