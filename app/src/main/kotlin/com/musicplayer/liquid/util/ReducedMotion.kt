package com.musicplayer.liquid.util

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Reduced Motion工具 - 检测系统动画偏好
 * 
 * 符合WCAG无障碍规范：
 * - prefers-reduced-motion 对应 Android Settings.Global.ANIMATOR_DURATION_SCALE
 * - scale < 0.5 视为用户明确要求减少动画
 */
object ReducedMotionUtils {
    
    /**
     * 检测系统是否启用了"减少动画"
     * 基于全局动画持续时间缩放系数（Settings.Global.ANIMATOR_DURATION_SCALE）
     * 
     * @return true = 应该减少动画（< 0.5x）；false = 正常动画
     */
    fun isReducedMotionEnabled(context: Context): Boolean {
        return try {
            val animatorScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            // 动画缩放 < 0.5 视为明确减少动画意图
            animatorScale < 0.5f
        } catch (e: Exception) {
            false // 默认启用动画
        }
    }
}

/**
 * Composable辅助函数 - 在组件中检测reduced-motion
 * 
 * 用法：
 * ```
 * val shouldReduceMotion = rememberReducedMotionPreference()
 * if (!shouldReduceMotion) {
 *     // 运行动画
 * }
 * ```
 */
@Composable
fun rememberReducedMotionPreference(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        ReducedMotionUtils.isReducedMotionEnabled(context)
    }
}
