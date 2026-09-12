package com.musicplayer.liquid.util

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import java.util.concurrent.ConcurrentHashMap

/**
 * Palette 结果缓存器
 * 避免重复提取相同图片的颜色
 */
object PaletteCache {
    private val cache = ConcurrentHashMap<String, Palette>()
    
    /**
     * 获取缓存的Palette,如果不存在则提取并缓存
     */
    fun getOrExtract(key: String, bitmap: Bitmap): Palette {
        return cache.getOrPut(key) {
            Palette.from(bitmap).generate()
        }
    }
    
    /**
     * 清空缓存(内存不足时可调用)
     */
    fun clear() {
        cache.clear()
    }
    
    /**
     * 获取缓存大小
     */
    fun size(): Int = cache.size
}
