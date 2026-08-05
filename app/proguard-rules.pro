-keep class us.zoom**{
    *;
}
-keep interface us.zoom**{
    *;
}

-keep class org.webrtc**{
    *;
}

-keep class com.zipow**{
    *;
}
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.gson.JsonObject
-dontwarn javax.annotation.**
-dontwarn java.awt.event.ActionListener
-dontwarn javax.swing.**
-dontwarn kotlin.coroutines.jvm.internal.SpillingKt
-dontwarn javax.lang.model.**