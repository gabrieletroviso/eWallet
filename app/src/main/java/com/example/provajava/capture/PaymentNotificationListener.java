package com.example.provajava.capture;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Receives every notification shown on the phone (once the user grants
 * "Accesso alle notifiche") and saves the ones coming from payment apps.
 * Everything stays on the phone.
 */
public class PaymentNotificationListener extends NotificationListenerService {

    private static final String TAG = "PaymentListener";

    // Same notification re-posted within this window is saved once
    private static final long DUPLICATE_WINDOW_MS = 10 * 60 * 1000;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        PaymentSource source = PaymentSource.fromPackage(sbn.getPackageName());
        if (source == null) return;

        Notification n = sbn.getNotification();
        // Group summaries just repeat the children ("2 nuovi messaggi")
        if ((n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;

        Bundle extras = n.extras;
        TCapturedNotification row = new TCapturedNotification();
        row.packageName = sbn.getPackageName();
        row.source = source.label;
        row.postedAt = sbn.getPostTime();
        row.title = asString(extras.getCharSequence(Notification.EXTRA_TITLE));
        row.text = asString(extras.getCharSequence(Notification.EXTRA_TEXT));
        row.bigText = asString(extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
        row.subText = asString(extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
        if (row.bigText != null && row.bigText.equals(row.text)) row.bigText = null;

        if (row.title == null && row.text == null && row.bigText == null) return;

        io.execute(() -> {
            try {
                CapturedNotificationDAO dao = CaptureDatabase.getInstance(this).getCapturedNotificationDAO();
                if (dao.countSame(row.packageName, row.title, row.text,
                        row.postedAt - DUPLICATE_WINDOW_MS) > 0) {
                    return;
                }
                dao.insert(row);
            } catch (Exception e) {
                Log.e(TAG, "Could not save notification", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }

    private static String asString(CharSequence cs) {
        if (cs == null) return null;
        String s = cs.toString().trim();
        return s.isEmpty() ? null : s;
    }

    /** True when the user has granted notification access to this app. */
    public static boolean isAccessGranted(Context context) {
        String enabled = Settings.Secure.getString(context.getContentResolver(),
                "enabled_notification_listeners");
        if (enabled == null) return false;
        ComponentName me = new ComponentName(context, PaymentNotificationListener.class);
        for (String flat : enabled.split(":")) {
            ComponentName cn = ComponentName.unflattenFromString(flat);
            if (me.equals(cn)) return true;
        }
        return false;
    }
}
