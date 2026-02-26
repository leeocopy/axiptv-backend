# Add project specific ProGuard rules here.
-keep class com.matrix.iptv.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
