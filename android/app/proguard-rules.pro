# ProGuard Rules for CSE HUB

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.csehub.app.**.model.** { *; }
-keep class com.csehub.app.**.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Firebase
-keep class com.google.firebase.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}

# Navigation
-keep class * extends androidx.fragment.app.Fragment{}
-keep class * extends android.app.Activity{}

# Keep model classes
-keep class com.csehub.app.core.network.models.** { *; }
-keep class com.csehub.app.auth.data.model.** { *; }
-keep class com.csehub.app.notification.data.model.** { *; }
-keep class com.csehub.app.event.data.model.** { *; }
-keep class com.csehub.app.timetable.data.model.** { *; }
-keep class com.csehub.app.file.data.model.** { *; }
-keep class com.csehub.app.gallery.data.model.** { *; }
-keep class com.csehub.app.profile.data.model.** { *; }
-keep class com.csehub.app.dashboard.data.model.** { *; }

# Prevent obfuscation of API response classes
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
