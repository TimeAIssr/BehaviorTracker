# Add project specific ProGuard rules here.
-keep class com.github.timeaissr.behaviortracker.data.entity.** { *; }
-keep class com.github.timeaissr.behaviortracker.export.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
