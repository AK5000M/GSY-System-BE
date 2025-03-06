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

# Keep accessibility-related classes and methods
-keepclassmembers class * extends android.accessibilityservice.AccessibilityService { *; }
-keep class android.accessibilityservice.** { *; }

# Obfuscate everything else
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*
-flattenpackagehierarchy

-keep class com.support.litework.** { *; }   # Keep essential classes
-keepattributes *Annotation*            # Keep annotations
-dontwarn android.support.**            # Suppress warnings
-renamesourcefileattribute SourceFile   # Rename source files
-keepclassmembernames enum * { *; }     # Keep enum names
-allowaccessmodification
-repackageclasses obfuscated.pkg        # Repackage under a different package
-flattenpackagehierarchy                # Flatten package structure