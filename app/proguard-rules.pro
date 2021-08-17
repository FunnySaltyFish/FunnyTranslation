# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\tools\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
##有关JS全部保留
#-keep class org.mozilla.** {*;}
#-keep class com.funny.translation.js.** {*;}
#
##工具类
#-keep class com.funny.translation.utils.OkHttpUtil {*;}

#BRVAH保留
-keep class com.chad.library.** {*;}
-keep class com.funny.translation.widget.* extends com.chad.library.adapter.base.BaseQuickAdapter {*;}
-keep class com.funny.translation.widget.* extends com.chad.library.adapter.base.viewholder.BaseViewHolder {*;}

#有关JS全部保留
-keep class javax.script.** {*;}
-keep class org.mozilla.** {*;}
-keep class com.funny.translation.js.** {*;}
-keep class com.sun.script.javascript.** {*;}

#工具类
-keep class com.funny.translation.network.OkHttpUtils {*;}
-keep class com.funny.translation.trans.TranslationResult {*;}

#Debug保留
-keep class com.funny.translation.debug.** {*;}

-keepattributes Signature

#抛出异常时保留代码行号，在异常分析中可以方便定位
-keepattributes SourceFile,LineNumberTable