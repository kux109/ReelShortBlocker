# Keep Accessibility Service classes
-keep class com.example.reelshortblocker.ReelShortBlockerService { *; }

# Keep Android accessibility APIs
-keep class android.accessibilityservice.** { *; }
-keep class android.view.accessibility.** { *; }

# Optimize aggressively
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}