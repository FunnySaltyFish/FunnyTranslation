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


#有关JS全部保留
-keep class javax.script.** {*;}
-keep class org.mozilla.** {*;}
-keep class com.funny.translation.js.** {*;}
-keep class com.sun.script.javascript.** {*;}
-keep class okhttp3.** {*;}

#工具类
-keep class com.funny.translation.network.OkHttpUtils {*;}
-keep class com.funny.translation.trans.TranslationResult {*;}
-keep class com.funny.translation.trans.Language {*;}

-keep class com.funny.translation.translate.engine.TranslationEngines {*;}
-keep interface com.funny.translation.trans.TranslationEngine {*;}
-keep class com.funny.translation.translate.bean.Consts {*;}

#Debug保留
-keep class com.funny.translation.debug.** {*;}

-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.funny.translation.translate.ui.thanks.Sponsor { *; }

#抛出异常时保留代码行号，在异常分析中可以方便定位
-keepattributes SourceFile,LineNumberTable