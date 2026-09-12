package com.musicplayer.liquid.util

/**
 * 时间格式化工具
 */
object TimeFormatter {
    /**
     * 将毫秒转换为 mm:ss 格式
     */
    fun formatTime(millis: Long): String {
        if (millis < 0) return "00:00"
        
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * 将毫秒转换为 h:mm:ss 格式（超过1小时）
     */
    fun formatTimeLong(millis: Long): String {
        if (millis < 0) return "00:00"
        
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
