package com.musicplayer.liquid

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.musicplayer.liquid.ui.components.ImageConfig
import dagger.hilt.android.HiltAndroidApp

/**
 * Application入口
 */
@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    
    override fun newImageLoader(): ImageLoader {
        return ImageConfig.createOptimizedImageLoader(this)
    }
}
