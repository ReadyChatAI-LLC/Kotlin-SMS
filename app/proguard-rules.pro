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


# Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class kotlinx.serialization.**{ *; }
-keep class kotlinx.serialization.json.**{ *; }

# Dagger Hilt
-keepnames @dagger.hilt.android.AndroidEntryPoint class *
-keepnames class * extends androidx.navigation.NavArgs
-keep class * extends androidx.navigation.NavType
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel *;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Navigation Compose
-keep class androidx.navigation.**{ *; }
-keepclassmembers class * implements androidx.navigation.NavArgs { *; }

# Coil
-keep class coil.**{ *; }
-keep class io.coil_kt.**{ *; }

# Libphonenumber
-keep class com.google.i18n.phonenumbers.**{ *; }

# DataStore
-keep class androidx.datastore.**{ *; }