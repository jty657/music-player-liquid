# Add project specific ProGuard rules here.

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep DataStore (for favorites persistence)
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Keep data classes (reflection used by DataStore serialization)
-keep class com.musicplayer.liquid.data.model.** { *; }
-keepclassmembers class com.musicplayer.liquid.data.model.** {
    <fields>;
    <init>(...);
}

# Keep Compose runtime (reflection)
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**

# Keep Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

# Optimize and obfuscate
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-repackageclasses ''
-allowaccessmodification
