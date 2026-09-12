package com.musicplayer.liquid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * 玻璃拟态卡片组件
 * 支持点击涟漪效果
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(
                            bounded = true,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        // 背景模糊层
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(10.dp)
                .background(Color.White.copy(alpha = 0.05f))
        )
        
        // 内容
        content()
    }
}
