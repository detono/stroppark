# Standard Android & Attributes
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses, SourceFile, LineNumberTable

# Kotlin Multiplatform / Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# Kotlinx Serialization
-keepclassmembers class fyi.tono.stroppark.** {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class fyi.tono.stroppark.** { *; }

# Room / Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Network (Ktor/OkHttp)
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer { *; }
-keepnames class okhttp3.internal.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Koin
-keepnames class org.koin.**
-keepclassmembers class org.koin.** { *; }
-dontwarn org.koin.**

-keepnames class fyi.tono.stroppark.**
-keepclassmembers class fyi.tono.stroppark.** {
    public <init>(...);
}

# Explicitly protect the reporter and utils
-keep class fyi.tono.stroppark.core.utils.CrashReporter { *; }
-keep class fyi.tono.stroppark.core.utils.** { *; }