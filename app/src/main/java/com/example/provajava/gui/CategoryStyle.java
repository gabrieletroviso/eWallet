package com.example.provajava.gui;

import android.content.res.ColorStateList;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.provajava.R;
import com.example.provajava.enumerator.eTranSubType;

// Icon and colors of each transaction category, as in the redesign.
// Shared by every list that shows transactions.
public final class CategoryStyle {

    @DrawableRes public final int icon;
    @ColorRes public final int iconColor;
    @ColorRes public final int tileColor;

    private CategoryStyle(@DrawableRes int icon, @ColorRes int iconColor, @ColorRes int tileColor) {
        this.icon = icon;
        this.iconColor = iconColor;
        this.tileColor = tileColor;
    }

    public static CategoryStyle of(eTranSubType subType) {
        if (subType == null) {
            return new CategoryStyle(R.drawable.ic_cat_necessaria, R.color.ew_text_secondary, R.color.ew_track);
        }
        switch (subType) {
            case NECESSARY:
                return new CategoryStyle(R.drawable.ic_cat_necessaria, R.color.ew_cat_necessaria_icon, R.color.ew_cat_necessaria_container);
            case UNNECESSARY:
                return new CategoryStyle(R.drawable.ic_cat_superflua, R.color.ew_cat_superflua_icon, R.color.ew_cat_superflua_container);
            case EXTRA:
                return new CategoryStyle(R.drawable.ic_cat_extra, R.color.ew_cat_extra_icon, R.color.ew_cat_extra_container);
            case SALARY:
                return new CategoryStyle(R.drawable.ic_cat_stipendio, R.color.ew_income, R.color.ew_income_container);
            case OTHER_INCOME:
                return new CategoryStyle(R.drawable.ic_arrow_in, R.color.ew_income, R.color.ew_income_container);
            case TRADE:
                return new CategoryStyle(R.drawable.ic_cat_trade, R.color.ew_primary_dark, R.color.ew_primary_container);
            default:
                return new CategoryStyle(R.drawable.ic_cat_necessaria, R.color.ew_text_secondary, R.color.ew_track);
        }
    }

    // Apply icon, icon color and tile color to an ImageView that has the bg_icon_tile background
    public void applyTo(ImageView view) {
        view.setImageResource(icon);
        ImageViewCompat.setImageTintList(view,
                ColorStateList.valueOf(ContextCompat.getColor(view.getContext(), iconColor)));
        ViewCompat.setBackgroundTintList(view,
                ColorStateList.valueOf(ContextCompat.getColor(view.getContext(), tileColor)));
    }
}
