
# leave stack traces and reflection intact
-dontobfuscate

# keep all model and util classes for parsing
-keep class com.hound.core.model.** { *; }
-keep class com.hound.core.util.** { *; }

# keep JNI classes
-keep class com.soundhound.android.libvad.** { *; }
-keep class com.hound.android.libphs.** { *; }
-keep class com.soundhound.android.libspeex.** { *; }
