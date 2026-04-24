package com.example.project666;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.project666.login.SignInActivity;


// It takes around 3-4 min to receive it sometimes sorry :<

/**
 * BroadcastReceiver that handles reminder alarms by building and showing
 * a notification which directs the user to sign in
 */
public class NotificationReceiver extends BroadcastReceiver{
    /** ID of the notification channel to post reminders on. */
    public static String CHANNEL_ID = "reminders_channel";

    /**
     * Called when a reminder alarm is fired
     * Extracts the reminder key from the Intent, builds a notification
     * with tap action to SignInActivity, and posts it if notification
     * permission is checked
     *
     * @param context the Context in which the receiver is running
     * @param intent  the broadcast Intent containing the "reminderKey" extra
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String key = intent.getStringExtra("reminderKey");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Time to check your reminder")
                .setContentText("Tap to sign in and review it.")
                .setAutoCancel(true);

        // When tapped, send the user to the SignInActivity
        Intent signIn = new Intent(context, SignInActivity.class);

        signIn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        builder.setContentIntent(
                PendingIntent.getActivity(context, 0, signIn,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)
        );

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context)
                    .notify(key.hashCode(), builder.build());
        }
    }

}
