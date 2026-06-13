# ProGuard rules for U-DAW

# Oboe library (audio engine)
-keep class com.google.oboe.** { *; }
-dontwarn com.google.oboe.**

# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep the NativeWavetableSynthesizer class and its members
-keep class com.chumakov123.udaw.NativeWavetableSynthesizer { *; }

# Kotlin Serialization (if used for complex data passing via JNI)
-keepattributes *Annotation*, EnclosingMethod, Signature, InnerClasses
-keep class kotlinx.serialization.json.** { *; }

# General Native Support
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
