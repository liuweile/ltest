# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\xieyongxiong\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-printmapping aar/release/mapping.txt
-optimizationpasses 5 # 指定代码的压缩级别
-dontusemixedcaseclassnames # 是否使用大小写混合
-obfuscationdictionary d1.txt
-classobfuscationdictionary d2.txt
-packageobfuscationdictionary d3.txt
-repackageclasses 'com.alxad.z'
-keepattributes Signature
-dontpreverify # 混淆时是否做预校验
-verbose # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/* # 混淆时所采用的算法
-keep public class * extends android.app.Activity # 保持哪些类不被混淆
-keep public class * extends android.app.Application # 保持哪些类不被混淆
-keep public class * extends android.app.Service # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference # 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService # 保持哪些类不被混淆
-keepclasseswithmembernames class * { # 保持 native 方法不被混淆
   native <methods>;
}
-keepclasseswithmembers class * { # 保持自定义控件类不被混淆
   public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
   public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
   public void *(android.view.View);
}
-keepclassmembers enum * { # 保持枚举 enum 类不被混淆
   public static **[] values();
   public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {#保持Parcelable不被混淆
   public static final android.os.Parcelable$Creator *;
}
# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keep public class * implements java.io.Serializable {*;}
-keepclassmembers class * implements java.io.Serializable {
   static final long serialVersionUID;
   private static final java.io.ObjectStreamField[]   serialPersistentFields;
   private void writeObject(java.io.ObjectOutputStream);
   private void readObject(java.io.ObjectInputStream);
   java.lang.Object writeReplace();
   java.lang.Object readResolve();
}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepattributes Signature



-keepclassmembers class * extends android.webkit.WebChromeClient {
       public void openFileChooser(...);
}


-keep public class * extends android.content.BroadcastReceiver


-dontwarn android.os.**
-dontwarn android.content.pm.**
-dontwarn android.os.**
-dontwarn android.content.pm.**
-dontnote android.support.**
-dontnote android.os.**

-dontnote com.google.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-dontnote android.support.**
-dontnote android.os.**
-dontwarn org.**
-dontwarn android.**

-keep class com.android.**{*;}
-keep class org.**{*;}
-keep class com.android.**{*;}
-keep class android.**{*;}


-dontwarn org.apache.http**
-keep class org.apache.http.**{*;}


-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**


-dontwarn org.apache.http**
-keep class org.apache.http.**{*;}

-keep public class com.android.vending.licensing.ILicensingService

-keep class com.android.vending.billing.**

# 保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

 # 保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# 序列化
-keep class * extends android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keep class com.alxad.api.** {*;}

