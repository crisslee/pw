# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
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

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-dontoptimize
-verbose

-dontnote org.apache.http.**
-dontnote org.apache.commons.**
-dontnote android.net.http.**

#gpns
-keep class com.goome.**{*;}

#以下两行bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
-keep class android.support.**{*;}

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep class * implements java.io.Serializable { *; }
-keep class * implements android.os.Parcelable { *; }
-keep class * implements android.os.Parcelable {              # aidl文件不能去混淆.
      public static final android.os.Parcelable$Creator *;
       }

-dontwarn android.support.v4.**
-dontwarn android.annotation
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    public void get*(...);
}
-keepclasseswithmembernames class * {            # 所有native的方法不能去混淆.
    native <methods>;
}
-keepclasseswithmembers class * {#某些构造方法不能去混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

#百度
-keep class com.baidu.** { *; }
-keep class mapsdkvi.com.**{*;}
-dontwarn com.baidu.**
-ignorewarnings

-keep class com.handmark.pulltorefresh.library.** {*;}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
# 谷歌
-keep class android.** {*;}

-keep public class com.coomix.app.all.R$*{
public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 高德
-keep class com.amap.api.**  {*;}
-keep class com.a.a.**  {*;}
-keep class com.autonavi.** {*;}
-dontwarn com.amap.api.**
-dontwarn com.a.a.**
-dontwarn com.autonavi.**

# umeng --- begin ---
-keep class com.umeng.** {*;}
# umeng --- end ---

#umeng分享(begin)
-dontshrink
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**
-keep public class com.umeng.socialize.* {*;}
-keep public class javax.**
-keep public class android.webkit.**
-keep public class com.tencent.** {*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
# umeng分享(end)

# 以下类过滤不混淆
-keep public class * extends com.umeng.**

-dontwarn com.mato.**
-keep class com.mato.**{ *;}
-dontwarn org.apache.**
-keep class org.apache.**{ *;}
-keep class u.**{ *;}
-keep class com.tencent.**{ *;}
-keep class com.google.gson.**{ *;}
-keep class com.google.a.**{ *;}
-keep class com.qq.taf.a.**{ *;}
-keep class com.handmark.pulltorefresh.library.**{ *;}
-keep class se.emilsjolander.stickylistheaders.**{ *;}
-keep class net.simonvt.numberpicker.**{ *;}
-keep class com.muzhi.camerasdk.**{ *;}
-keep class com.menu.**{ *;}
-keep class com.viewpagerindicator.**{ *;}
-keep class com.zhy.view.**{ *;}
-keep class kankan.wheel.widget.**{ *;}
-keep class kankan.wheel.widget.adapters.**{ *;}
-keep class com.fyales.tagcloud.library.**{ *;}
-keep class com.coomix.app.all.webview.**{ *;}
-dontwarn android.net.http.**
-keep class android.net.http.** { *;}

#ormlite start
-dontnote

-keepattributes Signature
-keep class ir.hnfadak.simineh.database.** { *; }

-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

-keepclassmembers class * {
  public <init>(android.content.Context);
}
#ormlite end

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# 腾讯地图

#腾讯地图 2D sdk
#-libraryjars libs/TencentLocationSDK_v4.4.6_r206631_151119_1441.jar
-keep class com.tencent.mapsdk.**{*;}
-keep class com.tencent.tencentmap.**{*;}
-keep class com.coomix.app.all.model.bean.**{*;}
-keep class com.coomix.app.all.model.bean.CommunityNotiBean$*{
    <methods>;
}

-keepclassmembers class com.coomix.app.all.model.bean.CommunityNotiBean$*{
    *;
}

-keep class com.coomix.app.all.model.bean.AlarmCategoryListBean$*{
    <methods>;
}

-keepclassmembers class com.coomix.app.all.model.bean.AlarmCategoryListBean$*{
    *;
}

#腾讯地图 3D sdk
#-libraryjars libs/TencentMapSDK_Raster_v1.1.2.16281.jar
-keep class com.tencent.map.**{*;}

#腾讯地图检索sdk
#-libraryjars libs/TencentSearch1.1.2.16095.jar
-keep class com.tencent.lbssearch.**{*;}
-keep class com.google.gson.examples.android.model.** { *; }

#-libraryjars libs/TencentStreetSDK_v.1.2.0_16324.jar
-keep class com.tencent.tencentmap.streetviewsdk.**{*;}

-keepclassmembers class ** {
    public void on*Event(...);
}
-keepclasseswithmembernames class * {
    native <methods>;
}
-dontwarn  org.eclipse.jdt.annotation.**

# sdk版本小于18时需要以下配置, 建议使用18或以上版本的sdk编译
-dontwarn  android.location.Location
-dontwarn  android.net.wifi.WifiManager

-dontnote ct.**

#eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

## RETROFIT
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

## OKHTTP
-dontwarn okio.**
-dontwarn javax.annotation.**

# keep model
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.coomix.app.all.model.** {*;}

# 微信
-keep class com.tencent.mm.opensdk.** {
   *;
}
-keep class com.tencent.wxop.** {
   *;
}
-keep class com.tencent.mm.sdk.** {
   *;
}

#****GM IM********START**********
-keep class net.goome.im.** {*;}
-dontwarn net.goome.im.**
-dontwarn me.leolin.shortcutbadger.**

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

# Keep SafeParcelable value, needed for reflection. This is required to support backwards
# compatibility of some classes.
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

# Keep the names of classes/members we need for client functionality.
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# retro lambda
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*