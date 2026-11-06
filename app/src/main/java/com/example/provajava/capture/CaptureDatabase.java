package com.example.provajava.capture;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Separate database for the captured notifications (and, later, the drafts to confirm).
 * Kept apart from the main eWallet database so that its schema version, the Drive
 * backup and the restore check are not affected.
 */
@androidx.room.Database(entities = {TCapturedNotification.class}, version = 1, exportSchema = false)
public abstract class CaptureDatabase extends RoomDatabase {

    private static final String NAME = "capture.db";
    private static volatile CaptureDatabase INSTANCE;

    public abstract CapturedNotificationDAO getCapturedNotificationDAO();

    public static CaptureDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CaptureDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CaptureDatabase.class, NAME).build();
                }
            }
        }
        return INSTANCE;
    }
}
