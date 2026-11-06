package com.example.provajava.capture;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A notification from a payment app, saved exactly as received.
 * It is the raw material for the parsers: nothing is interpreted here.
 */
@Entity(tableName = "T_CapturedNotification")
public class TCapturedNotification {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "PackageName")
    public String packageName;

    // Label of the PaymentSource (Fineco, BPER, ...)
    @ColumnInfo(name = "Source")
    public String source;

    // When the app posted the notification (epoch millis)
    @ColumnInfo(name = "PostedAt")
    public long postedAt;

    @ColumnInfo(name = "Title")
    public String title;

    @ColumnInfo(name = "Text")
    public String text;

    // Expanded text, often longer than Text (null when the app does not set it)
    @ColumnInfo(name = "BigText")
    public String bigText;

    @ColumnInfo(name = "SubText")
    public String subText;
}
