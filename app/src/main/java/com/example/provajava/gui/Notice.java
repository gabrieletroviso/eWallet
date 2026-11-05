package com.example.provajava.gui;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.provajava.R;

// Short messages of the app in the redesign style (they replace the system Toasts):
// a white rounded card with a colored icon, shown at the bottom of the page, above the bottom bar.
//   Notice.error(activity, "...")    red ✕, stays longer
//   Notice.success(activity, "...")  green ✓
//   Notice.info(activity, "...")     neutral i
// The card is a plain view added to the page's own content area (no Snackbar), so it always stays
// inside the screen, and a new message replaces the previous one instead of queueing.
public final class Notice {

    private static final String TAG = "ewallet_notice";
    private static final long SHORT_MS = 2200;
    private static final long LONG_MS = 4000;

    private Notice() {}

    public static void error(@Nullable Activity activity, String message) {
        show(contentOf(activity), activity, message, R.drawable.ic_notice_error,
                R.color.ew_expense, R.color.ew_expense_container, LONG_MS);
    }

    public static void success(@Nullable Activity activity, String message) {
        show(contentOf(activity), activity, message, R.drawable.ic_notice_success,
                R.color.ew_income, R.color.ew_income_container, SHORT_MS);
    }

    public static void info(@Nullable Activity activity, String message) {
        show(contentOf(activity), activity, message, R.drawable.ic_notice_info,
                R.color.ew_primary_dark, R.color.ew_primary_container, SHORT_MS);
    }

    // Variant for messages raised inside a dialog: shown at the bottom of the dialog itself
    public static void error(View dialogView, String message) {
        View content = dialogView.getRootView().findViewById(android.R.id.content);
        show(content instanceof FrameLayout ? (FrameLayout) content : null, null, message,
                R.drawable.ic_notice_error, R.color.ew_expense, R.color.ew_expense_container, LONG_MS);
    }

    @Nullable
    private static FrameLayout contentOf(@Nullable Activity activity) {
        if (activity == null || activity.isFinishing()) return null;
        View content = activity.findViewById(android.R.id.content);
        return content instanceof FrameLayout ? (FrameLayout) content : null;
    }

    private static void show(@Nullable FrameLayout content, @Nullable Activity activity, String message,
                             @DrawableRes int icon, @ColorRes int iconColor, @ColorRes int tileColor, long durationMs) {
        if (content == null) return;

        // Only one message at a time
        View previous = content.findViewWithTag(TAG);
        if (previous != null) content.removeView(previous);

        View card = LayoutInflater.from(content.getContext()).inflate(R.layout.notice, content, false);
        card.setTag(TAG);
        ((TextView) card.findViewById(R.id.noticeText)).setText(message);
        ImageView iv = card.findViewById(R.id.noticeIcon);
        iv.setImageResource(icon);
        ImageViewCompat.setImageTintList(iv,
                ColorStateList.valueOf(ContextCompat.getColor(content.getContext(), iconColor)));
        ViewCompat.setBackgroundTintList(iv,
                ColorStateList.valueOf(ContextCompat.getColor(content.getContext(), tileColor)));

        // Full width minus the page margins, at the bottom; above the bottom bar when there is one
        int side = dp(content, 16);
        int bottom = dp(content, 12);
        if (activity != null) {
            View nav = activity.findViewById(R.id.bottomNavBar);
            if (nav != null && nav.getVisibility() == View.VISIBLE) bottom += nav.getHeight();
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        lp.setMargins(side, 0, side, bottom);
        content.addView(card, lp);

        // Slide up + fade in, then out after the duration (or on tap)
        card.setAlpha(0f);
        card.setTranslationY(dp(content, 16));
        card.animate().alpha(1f).translationY(0f).setDuration(180).start();

        Runnable hide = () -> card.animate().alpha(0f).translationY(dp(content, 16)).setDuration(180)
                .withEndAction(() -> {
                    if (card.getParent() == content) content.removeView(card);
                }).start();
        card.setOnClickListener(v -> hide.run());
        card.postDelayed(hide, durationMs);
    }

    private static int dp(View v, int value) {
        return Math.round(value * v.getResources().getDisplayMetrics().density);
    }
}
