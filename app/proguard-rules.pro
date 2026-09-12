# Add project specific ProGuard rules here.

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep data classes
-keep class com.musicplayer.liquid.data.model.** { *; }
