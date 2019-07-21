# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/Program Files (x86)/Android/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class firstLine to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-android
-dontpreverify
-repackageclasses ''
-allowaccessmodification

# fastjson proguard rules
# https://github.com/alibaba/fastjson

-dontwarn com.alibaba.fastjson.**
-keepattributes Signature
-keepattributes *Annotation*

## Joda Time 2.3

-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# Logback for Android
#
# Tested on the following *.gradle dependencies
#
#    compile 'org.slf4j:slf4j-api:1.7.7'
#    compile 'com.github.tony19:logback-android-core:1.1.1-3'
#    compile 'com.github.tony19:logback-android-classic:1.1.1-3'
#

#-keep class ch.qos.** { *; }
#-keep class org.slf4j.** { *; }
#-keepattributes *Annotation*
#-dontwarn ch.qos.logback.core.net.*

-dontwarn javax.naming.**
-dontwarn javax.servlet.**
-dontwarn org.slf4j.**

#build.gradle
#
#    compile 'io.reactivex:rxandroid:1.0.1'
#    compile 'io.reactivex:rxjava:1.0.14'
#    compile 'io.reactivex:rxjava-math:1.0.0'
#    compile 'com.jakewharton.rxbinding:rxbinding:0.2.0'

# rxjava
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

#rx.internal rules

-dontwarn sun.misc.Unsafe
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

#guava
-dontwarn com.google.common.base.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn java.lang.ClassValue
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# Added for guava 23.5-android
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**