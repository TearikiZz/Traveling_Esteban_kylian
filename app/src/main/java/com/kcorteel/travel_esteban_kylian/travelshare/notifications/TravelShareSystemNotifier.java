package com.kcorteel.travel_esteban_kylian.travelshare.notifications;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.kcorteel.travel_esteban_kylian.NotificationsActivity;
import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.TravelShareDetailActivity;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Notification;

public final class TravelShareSystemNotifier {

    public static final String CHANNEL_ID = "travelshare_activity";

    private TravelShareSystemNotifier() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notifications_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.notifications_channel_description));
        manager.createNotificationChannel(channel);
    }

    public static void requestPermissionIfNeeded(
            Activity activity,
            ActivityResultLauncher<String> permissionLauncher
    ) {
        if (activity == null || permissionLauncher == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    public static boolean canPostNotifications(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean showNotification(Context context, Notification notification, String title) {
        if (context == null || notification == null || !canPostNotifications(context)) {
            return false;
        }

        ensureChannel(context);

        Intent detailIntent = new Intent(context, TravelShareDetailActivity.class);
        detailIntent.putExtra(TravelShareDetailActivity.EXTRA_PHOTO_ID, notification.getRelatedPhotoId());
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent detailPendingIntent = PendingIntent.getActivity(
                context,
                (int) notification.getNotifId(),
                detailIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent notificationsIntent = new Intent(context, NotificationsActivity.class);
        notificationsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent notificationsPendingIntent = PendingIntent.getActivity(
                context,
                (int) notification.getNotifId() + 10_000,
                notificationsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(notification.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(detailPendingIntent)
                .addAction(
                        0,
                        context.getString(R.string.profile_notifications_button),
                        notificationsPendingIntent
                );

        NotificationManagerCompat.from(context).notify((int) notification.getNotifId(), builder.build());
        return true;
    }
}
