package com.musicplayer.liquid.util

/**
 * 时间格式化工具
 */
object TimeFormatter {
    /**
     * 将毫秒转换为 mm:ss 格式
     */
    fun formatTime(millis: Long): String {
        val totalSeconds = (millis / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
