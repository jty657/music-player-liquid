package com.musicplayer.liquid.data.model

/**
 * 播放模式
 */
enum class PlaybackMode {
    /** 顺序播放 */
    SEQUENTIAL,
    /** 单曲循环 */
    REPEAT_ONE,
    /** 列表循环 */
    REPEAT_ALL,
    /** 随机播放 */
    SHUFFLE
}
