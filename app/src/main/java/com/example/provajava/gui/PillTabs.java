package com.example.provajava.gui;

import android.graphics.Typeface;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.provajava.R;

// "Pill" tabs of the redesign (Riepilogo/Movimenti, Andamento/Spese/...), kept in sync with a ViewPager2:
// tapping a tab moves the pager, swiping the pager moves the selection. Tab i shows page i.
public final class PillTabs {

    private PillTabs() {}

    public static void bind(ViewPager2 pager, TextView... tabs) {
        for (int i = 0; i < tabs.length; i++) {
            final int page = i;
            tabs[i].setOnClickListener(v -> pager.setCurrentItem(page, true));
        }
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < tabs.length; i++) {
                    style(tabs[i], i == position);
                }
            }
        });
        for (int i = 0; i < tabs.length; i++) {
            style(tabs[i], i == pager.getCurrentItem());
        }
    }

    private static void style(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.bg_tab_selected : 0);
        tab.setElevation(selected ? tab.getResources().getDisplayMetrics().density : 0);
        tab.setTextColor(ContextCompat.getColor(tab.getContext(),
                selected ? R.color.ew_text : R.color.ew_text_secondary));
        // Design: selected tab Nunito 800, the others 700
        tab.setTypeface(selected
                ? ResourcesCompat.getFont(tab.getContext(), R.font.nunito_extrabold)
                : Typeface.create(ResourcesCompat.getFont(tab.getContext(), R.font.nunito), Typeface.BOLD));
        tab.setSelected(selected);
    }
}
