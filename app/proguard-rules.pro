# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile



# Keep application classes and methods
-keep class com.yantraman.objectscanner.** { *; }

# Keep ARCore & Sceneform UX classes
-keep class com.google.ar.core.** { *; }
-keep class com.google.ar.sceneform.** { *; }

# Keep Material and AppCompat classes
-keep class androidx.appcompat.** { *; }
-keep class com.google.android.material.** { *; }

# Keep Generative AI client library classes
-keep class com.google.ai.client.generativeai.** { *; }

# Google Play Services and Firebase (if used)
-keep class com.google.** { *; }


# Keep classes with @Keep annotation
-keep @androidx.annotation.Keep class * { *; }

# Keep serialized classes
-keepnames class * implements java.io.Serializable

# Prevent code obfuscation of JSON keys
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
