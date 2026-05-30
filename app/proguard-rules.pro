# Keep all Gson DTO classes (wrong package name caused the crash)
-keep class com.tamilbookreader.app.data.** { *; }
-keep class com.tamilbookreader.app.domain.** { *; }

# Gson internals
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# AdMob
-keep class com.google.android.gms.ads.** { *; }
