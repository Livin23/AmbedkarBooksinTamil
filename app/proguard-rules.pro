# Keep all Gson DTO and domain classes (must match actual package)
-keep class com.livin.ambedkarindhiavilsathigal.data.** { *; }
-keep class com.livin.ambedkarindhiavilsathigal.domain.** { *; }

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
