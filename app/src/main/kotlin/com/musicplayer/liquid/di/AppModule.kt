package com.musicplayer.liquid.di

import android.content.Context
import com.musicplayer.liquid.data.player.ExoPlayerImpl
import com.musicplayer.liquid.data.player.MusicPlayer
import com.musicplayer.liquid.data.repository.MusicRepository
import com.musicplayer.liquid.data.repository.MusicRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    
    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        impl: MusicRepositoryImpl
    ): MusicRepository
    
    @Binds
    @Singleton
    abstract fun bindMusicPlayer(
        impl: ExoPlayerImpl
    ): MusicPlayer
}
