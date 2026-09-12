package com.musicplayer.liquid.data.model

/**
 * 睡眠定时器配置
 */
data class SleepTimer(
    val enabled: Boolean = false,
    val remainingMillis: Long = 0L,
    val totalMillis: Long = 0L
) {
    companion object {
        const val TIMER_5_MIN = 5 * 60 * 1000L
        const val TIMER_10_MIN = 10 * 60 * 1000L
        const val TIMER_15_MIN = 15 * 60 * 1000L
        const val TIMER_30_MIN = 30 * 60 * 1000L
        const val TIMER_60_MIN = 60 * 60 * 1000L
    }
}
