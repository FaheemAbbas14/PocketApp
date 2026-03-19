# Keep runtime-visible annotations used by Android/Firebase tooling.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Preserve Firestore model members that are mapped by name.
-keepclassmembers class com.faheemlabs.pocketapp.TaskItem { *; }
-keepclassmembers class com.faheemlabs.pocketapp.ExpenseItem { *; }
-keepclassmembers class com.faheemlabs.pocketapp.EventItem { *; }
-keepclassmembers class com.faheemlabs.pocketapp.PaymentItem { *; }

# Keep classes implementing Firebase messaging/service callbacks if added later.
-keep public class * extends com.google.firebase.messaging.FirebaseMessagingService
-keep public class * extends com.google.firebase.iid.FirebaseInstanceIdService

# Remove verbose logs from release bytecode.
-assumenosideeffects class android.util.Log {
	public static int v(...);
	public static int d(...);
	public static int i(...);
	public static int w(...);
}
