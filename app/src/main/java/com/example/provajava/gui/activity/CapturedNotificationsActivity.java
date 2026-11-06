package com.example.provajava.gui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.provajava.R;
import com.example.provajava.capture.CaptureDatabase;
import com.example.provajava.capture.CapturedNotificationDAO;
import com.example.provajava.capture.TCapturedNotification;
import com.example.provajava.gui.ConfirmDialog;
import com.example.provajava.gui.Notice;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shows the raw notifications saved by PaymentNotificationListener.
 * "Copia tutto" puts them in the clipboard as plain text, to build the parsers from real examples.
 */
public class CapturedNotificationsActivity extends AppCompatActivity {

    private static final DateTimeFormatter WHEN =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private CapturedNotificationDAO dao;
    private List<TCapturedNotification> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.captured_notifications);
        dao = CaptureDatabase.getInstance(this).getCapturedNotificationDAO();

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findViewById(R.id.copyCapturedBtn).setOnClickListener(v -> copyAll());
        findViewById(R.id.clearCapturedBtn).setOnClickListener(v -> ConfirmDialog.destructive(this,
                R.drawable.ic_trash,
                "Svuotare le notifiche raccolte?",
                "Le notifiche salvate finora verranno eliminate. I movimenti non vengono toccati.",
                "Svuota",
                this::clearAll));
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }

    private void load() {
        io.execute(() -> {
            List<TCapturedNotification> all = dao.getAll();
            runOnUiThread(() -> show(all));
        });
    }

    private void show(List<TCapturedNotification> all) {
        if (isFinishing()) return;
        items = all;
        LinearLayout list = findViewById(R.id.capturedList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (TCapturedNotification n : all) {
            View row = inflater.inflate(R.layout.captured_notification_row, list, false);
            ((TextView) row.findViewById(R.id.capSource)).setText(
                    n.source + " · " + WHEN.format(Instant.ofEpochMilli(n.postedAt)));
            TextView title = row.findViewById(R.id.capTitle);
            title.setText(n.title);
            title.setVisibility(n.title == null ? View.GONE : View.VISIBLE);
            ((TextView) row.findViewById(R.id.capBody)).setText(body(n));
            list.addView(row);
        }

        boolean empty = all.isEmpty();
        findViewById(R.id.capturedEmpty).setVisibility(empty ? View.VISIBLE : View.GONE);
        ((View) list.getParent()).setVisibility(empty ? View.GONE : View.VISIBLE);
        findViewById(R.id.copyCapturedBtn).setEnabled(!empty);
        findViewById(R.id.clearCapturedBtn).setEnabled(!empty);
        ((TextView) findViewById(R.id.capturedCount)).setText(
                all.size() == 1 ? "1 notifica" : all.size() + " notifiche");
    }

    private static String body(TCapturedNotification n) {
        StringBuilder sb = new StringBuilder();
        if (n.text != null) sb.append(n.text);
        if (n.bigText != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(n.bigText);
        }
        if (n.subText != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("[").append(n.subText).append("]");
        }
        return sb.toString();
    }

    private void copyAll() {
        StringBuilder sb = new StringBuilder();
        for (TCapturedNotification n : items) {
            sb.append("--- ").append(n.source).append(" (").append(n.packageName).append(") ")
                    .append(WHEN.format(Instant.ofEpochMilli(n.postedAt))).append('\n');
            sb.append("title: ").append(n.title).append('\n');
            sb.append("text: ").append(n.text).append('\n');
            if (n.bigText != null) sb.append("bigText: ").append(n.bigText).append('\n');
            if (n.subText != null) sb.append("subText: ").append(n.subText).append('\n');
            sb.append('\n');
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Notifiche eWallet", sb.toString()));
        Notice.success(this, "Notifiche copiate");
    }

    private void clearAll() {
        io.execute(() -> {
            dao.deleteAll();
            runOnUiThread(() -> {
                Notice.success(this, "Notifiche eliminate");
                load();
            });
        });
    }
}
