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
 * 液态玻璃拟态卡片组件 - v4.0 高级液态版
 * 
 * 符合 Emil Kowalski 设计哲学：
 * - 极致液态玻璃效果：垂直渐变背景 + 高光边框 + 径向模糊 + Specular高光
 * - 适度模糊（8dp）保证文字可读性
 * - 高频组件（列表卡片）避免无限循环动画
 * - 仅保留交互时的ripple反馈
 * 
 * 可选特性：
 * - enableLiquidBorder: 启用装饰性流动边框（默认关闭，避免高频组件性能开销）
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
            // 主毛玻璃层：垂直渐变（深色模式下增强对比）
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),  // 顶部更明亮
                        Color.White.copy(alpha = 0.08f),  // 中间透明
                        Color.White.copy(alpha = 0.14f)   // 底部微光
                    )
                ),
                shape = shape
            )
            // 高光边框：上方高光，下方柔和
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),   // 顶部强高光
                        Color.White.copy(alpha = 0.15f),  // 中间过渡
                        Color.White.copy(alpha = 0.05f)   // 底部淡化
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
        // 径向模糊层 + Specular高光（适度8dp，平衡美观与可读性）
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(8.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),   // 中心高光
                            Color.White.copy(alpha = 0.05f),  // 中间过渡
                            Color.Transparent               // 边缘透明
                        )
                        // center 默认为 Offset.Unspecified，自动居中
                    )
                )
        )
        
        // 内容（在所有效果层之上）
        content()
    }
}
