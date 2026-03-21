# Standard Android rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod,InnerClasses,SourceFile,LineNumberTable

# Kotlin Multiplatform / Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# Compose (Usually handled by Gradle, but safe to have)
-dontwarn androidx.compose.ui.platform.**

# Kotlinx Serialization (If you use it for APIs)
-keepclassmembers class fyi.tono.stroppark.** {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class fyi.tono.stroppark.** { *; }

# Room / Database (Based on your logs showing AppDatabase)
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Ktor engine discovery (uses ServiceLoader reflection)
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer { *; }
-keepclassmembers class io.ktor.client.engine.okhttp.OkHttpEngine { *; }

# OkHttp internals that get stripped
-keepnames class okhttp3.internal.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**