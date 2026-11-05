package com.example.provajava.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.enumerator.eTranMainType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

// List of transactions grouped by day, as in the redesign (Home · Movimenti, Ricerca):
// one section per day with its total, then a card with one row per transaction.
// Transactions must already be sorted by date (any direction).
public final class MovementList {

    private MovementList() {}

    // withYear: add the year to the day label (useful when the list spans more than a month)
    public static void render(LinearLayout container, List<TTransaction> transactions, boolean withYear,
                              Consumer<TTransaction> onClick) {
        Context ctx = container.getContext();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        container.removeAllViews();

        LocalDate currentDay = null;
        LinearLayout rows = null;
        TextView dayTotal = null;
        double total = 0;

        for (TTransaction t : transactions) {
            LocalDate date = Instant.ofEpochMilli(t.getDate()).atZone(ZoneId.systemDefault()).toLocalDate();

            if (!date.equals(currentDay)) {
                if (dayTotal != null) dayTotal.setText(Tools.formatEuro(total, true));
                View section = inflater.inflate(R.layout.movement_day, container, false);
                ((TextView) section.findViewById(R.id.dayLabel)).setText(dayLabel(date, withYear));
                dayTotal = section.findViewById(R.id.dayTotal);
                rows = section.findViewById(R.id.dayRows);
                container.addView(section);
                currentDay = date;
                total = 0;
            }

            total += signedAmount(t);
            rows.addView(buildRow(t, inflater, rows, onClick));
        }
        if (dayTotal != null) dayTotal.setText(Tools.formatEuro(total, true));
    }

    private static View buildRow(TTransaction tran, LayoutInflater inflater, ViewGroup parent,
                                 Consumer<TTransaction> onClick) {
        View row = inflater.inflate(R.layout.movement_row, parent, false);

        CategoryStyle.of(tran.getTranSubType()).applyTo(row.findViewById(R.id.rowIcon));
        ((TextView) row.findViewById(R.id.rowCategory)).setText(tran.getTranSubType().toString());

        TextView desc = row.findViewById(R.id.rowDescription);
        String d = tran.getDescription();
        if (d != null && !d.trim().isEmpty()) {
            desc.setText(d.trim());
            desc.setVisibility(View.VISIBLE);
        }

        TextView amount = row.findViewById(R.id.rowAmount);
        amount.setText(Tools.formatEuro(signedAmount(tran), true));
        amount.setTextColor(ContextCompat.getColor(parent.getContext(),
                isExpense(tran) ? R.color.ew_text : R.color.ew_income));

        if (onClick != null) {
            row.setOnClickListener(v -> onClick.accept(tran));
        } else {
            row.setClickable(false);
            row.setBackground(null);
        }
        return row;
    }

    // Expenses are stored as positive amounts: show them as negative
    public static double signedAmount(TTransaction t) {
        return isExpense(t) ? -t.getAmount() : t.getAmount();
    }

    private static boolean isExpense(TTransaction t) {
        return t.getTranMainType().equals(eTranMainType.EXPENSE);
    }

    // "Domenica 1 novembre" (or "Domenica 1 novembre 2026")
    private static String dayLabel(LocalDate date, boolean withYear) {
        String dow = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        String label = dow.substring(0, 1).toUpperCase(Locale.ITALIAN) + dow.substring(1)
                + " " + date.getDayOfMonth() + " " + month;
        return withYear ? label + " " + date.getYear() : label;
    }
}
