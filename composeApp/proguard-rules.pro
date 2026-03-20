# 1. Standard Android rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod,InnerClasses,SourceFile,LineNumberTable

# 2. Kotlin Multiplatform / Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# 3. Compose (Usually handled by Gradle, but safe to have)
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.ui.platform.**

# 4. Kotlinx Serialization (If you use it for APIs)
-keepattributes *Annotation*,EnclosingMethod,InnerClasses
-keepclassmembers class fyi.tono.stroppark.** {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class fyi.tono.stroppark.** { *; }

# 5. Room / Database (Based on your logs showing AppDatabase)
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
