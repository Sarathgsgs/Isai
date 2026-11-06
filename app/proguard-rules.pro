# Isai Proguard / R8 Rules

# Media3
-keep class androidx.media3.session.** { *; }
-dontwarn androidx.media3.session.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-keep class * extends dagger.hilt.internal.GeneratedComponentManager
-keep class * extends dagger.hilt.internal.ComponentManager
-keep class * implements dagger.hilt.internal.GeneratedComponent

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Glance
-keep class androidx.glance.** { *; }

