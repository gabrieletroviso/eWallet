package com.example.provajava.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.provajava.R;
import com.google.android.material.button.MaterialButton;

// Confirmation window in the redesign style (it replaces the system AlertDialogs):
// a rounded card with a colored icon, title, message, "Annulla" and the confirm button.
//   ConfirmDialog.destructive(...)  red, for actions that delete data
//   ConfirmDialog.normal(...)       blue, for the other confirmations
public final class ConfirmDialog {

    private ConfirmDialog() {}

    public static void destructive(Context ctx, @DrawableRes int icon, String title, String message,
                                   String confirmLabel, Runnable onConfirm) {
        show(ctx, icon, title, message, confirmLabel, R.color.ew_expense, R.color.ew_expense_container, onConfirm);
    }

    public static void normal(Context ctx, @DrawableRes int icon, String title, String message,
                              String confirmLabel, Runnable onConfirm) {
        show(ctx, icon, title, message, confirmLabel, R.color.ew_primary, R.color.ew_primary_container, onConfirm);
    }

    private static void show(Context ctx, @DrawableRes int icon, String title, String message, String confirmLabel,
                             @ColorRes int color, @ColorRes int tile, Runnable onConfirm) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.confirm_dialog, null, false);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        if (dialog.getWindow() != null) {
            // Our card draws its own background and rounded corners
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        int c = ContextCompat.getColor(ctx, color);
        ImageView iv = view.findViewById(R.id.confirmIcon);
        iv.setImageResource(icon);
        ImageViewCompat.setImageTintList(iv, ColorStateList.valueOf(c));
        ViewCompat.setBackgroundTintList(iv, ColorStateList.valueOf(ContextCompat.getColor(ctx, tile)));

        ((TextView) view.findViewById(R.id.confirmTitle)).setText(title);
        ((TextView) view.findViewById(R.id.confirmMessage)).setText(message);

        MaterialButton ok = view.findViewById(R.id.confirmOk);
        ok.setText(confirmLabel);
        ok.setBackgroundTintList(ColorStateList.valueOf(c));
        ok.setOnClickListener(v -> {
            dialog.dismiss();
            onConfirm.run();
        });
        view.findViewById(R.id.confirmCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Use most of the screen width (the default AlertDialog width is too narrow for our buttons)
        if (dialog.getWindow() != null) {
            int screen = ctx.getResources().getDisplayMetrics().widthPixels;
            int max = Math.round(420 * ctx.getResources().getDisplayMetrics().density);
            dialog.getWindow().setLayout(Math.min(Math.round(screen * 0.9f), max),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
