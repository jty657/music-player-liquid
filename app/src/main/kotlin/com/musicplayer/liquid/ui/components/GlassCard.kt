package com.musicplayer.liquid.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

/**
 * 液态玻璃拟态卡片组件 - v3.0 高性能优化版
 * 
 * 去除装饰性循环动画，专注核心液态玻璃效果：
 * - 三层毛玻璃：垂直渐变背景 + 径向模糊 + 高光边框
 * - 适度模糊（8dp）保证文字可读性
 * - 仅保留交互时的ripple反馈
 * 
 * 符合Emil设计规范：高频组件（列表卡片）避免无限循环动画
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    enableLiquidBorder: Boolean = false,  // 默认关闭装饰边框
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .clip(shape)
            // 主毛玻璃层：垂直渐变
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.12f)
                    )
                ),
                shape = shape
            )
            // 高光边框
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(
                            bounded = true,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        // 径向模糊层（适度8dp，平衡美观与可读性）
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(8.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // 内容（在所有效果层之上）
        content()
    }
}
