
# KISAN 365 ProGuard Rules

# Keep application classes
-keep class com.aone.kisan365.** { *; }

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application

# Preserve annotations
-keepattributes *Annotation*

