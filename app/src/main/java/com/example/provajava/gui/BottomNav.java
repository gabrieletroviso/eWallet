package com.example.provajava.gui;

import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.provajava.R;
import com.example.provajava.gui.activity.SearchActivityPage;
import com.example.provajava.gui.activity.StatisticsActivityPage;
import com.example.provajava.gui.activity.TodayActivityPage;
import com.example.provajava.gui.activity.ToolActivityPage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Bottom navigation shared by the four main pages.
// Home (TodayActivityPage) is the root of the stack: the other tabs open above it and replace
// each other, so "back" from any tab returns to Home and the stack never grows.
public final class BottomNav {

    private BottomNav() {}

    public static void setup(AppCompatActivity activity, @IdRes int selectedItem) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        nav.setSelectedItemId(selectedItem);
        nav.setOnItemReselectedListener(item -> { /* already here */ });
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() != selectedItem) {
                navigate(activity, item.getItemId(), selectedItem == R.id.nav_home);
            }
            return true;
        });
    }

    private static void navigate(AppCompatActivity from, @IdRes int itemId, boolean fromHome) {
        Class<?> target;
        if (itemId == R.id.nav_stats) {
            target = StatisticsActivityPage.class;
        } else if (itemId == R.id.nav_search) {
            target = SearchActivityPage.class;
        } else if (itemId == R.id.nav_settings) {
            target = ToolActivityPage.class;
        } else {
            target = TodayActivityPage.class;
        }

        Intent intent = new Intent(from, target);
        if (target == TodayActivityPage.class) {
            // Clear the stack down to Home and recreate it, so it shows fresh data
            // (transactions may have been changed from another tab, e.g. Cerca)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        from.startActivity(intent);
        if (!fromHome) {
            from.finish();
        }
        from.overridePendingTransition(0, 0);
    }
}
