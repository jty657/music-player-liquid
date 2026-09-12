package com.musicplayer.liquid.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

/**
 * 全局动画配置常量
 * 符合Emil Kowalski设计原则
 */
object AnimationConstants {
    // 动画时长（毫秒）
    const val PRESS_DURATION = 100
    const val ENTRANCE_DURATION = 200
    const val MODAL_DURATION = 300
    const val STAGGER_DELAY = 30
    const val MAX_STAGGER_DELAY = 150
    
    // Scale values
    const val PRESS_SCALE = 0.97f
    const val ENTRANCE_SCALE = 0.95f
    
    // Easing curves
    val EASE_OUT = LinearOutSlowInEasing
    val EASE_DRAWER = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
    val EASE_SPRING = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    
    // Opacity
    const val ENTRANCE_OPACITY = 0f
    const val SETTLED_OPACITY = 1f
}
