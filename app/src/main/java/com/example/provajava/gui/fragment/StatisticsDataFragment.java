package com.example.provajava.gui.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.gui.CategoryStyle;
import com.example.provajava.gui.activity.StatisticsActivityPage;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

// Statistiche · Medie: monthly averages of the selected year and average split of the expenses
public class StatisticsDataFragment extends Fragment implements iFragmentManaged {

    private StatisticsActivityPage listner;
    private View view;

    @Override
    public void onFragmentDismissed() {}

    @Override
    public void onFragmentClick(long id) {}

    @Override
    public void onFragmentNewTransaction(TTransaction tr) {}

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.statisticsdata, container, false);
        populate();
        return view;
    }

    // Called by the page when the year changes
    public void onChangeYear(){
        if (view != null) populate();
    }

    public void setListner(StatisticsActivityPage listner) {
        this.listner = listner;
    }

    private void populate(){
        populateGlobalStats();
        populateExpStats();
    }

    // ---------------------------------------------------------------- Medie mensili globali

    private void populateGlobalStats(){
        TYear y = listner.year;
        double inc = y == null ? 0 : y.getMeanIncome();
        double exp = y == null ? 0 : y.getMeanExpenses();
        double bal = y == null ? 0 : y.getMeanBalance();
        double proj = y == null ? 0 : y.getBalanceProjection();

        ((TextView) view.findViewById(R.id.incMeanVal)).setText(Tools.formatEuro(inc));
        ((TextView) view.findViewById(R.id.expMeanVal)).setText(Tools.formatEuro(exp));

        // Bilancio: green card when positive, red when negative
        boolean negative = Math.round(bal * 100) < 0;
        ViewCompat.setBackgroundTintList(view.findViewById(R.id.balMeanCard), ColorStateList.valueOf(
                color(negative ? R.color.ew_expense_container : R.color.ew_income_container)));
        int onBal = color(negative ? R.color.ew_on_expense_container : R.color.ew_on_income_container);
        ((TextView) view.findViewById(R.id.balMeanLbl)).setTextColor(onBal);
        TextView balVal = view.findViewById(R.id.balMeanVal);
        balVal.setTextColor(onBal);
        balVal.setText(Tools.formatEuro(bal, true));

        // Proiezione annua: primary card with white text
        ((TextView) view.findViewById(R.id.projLbl)).setTextColor(0xE0FFFFFF);
        TextView projVal = view.findViewById(R.id.projVal);
        projVal.setTextColor(color(R.color.white));
        projVal.setText(Tools.formatEuro(proj, true));
    }

    // ------------------------------------------------------------------- Spese medie mensili

    private void populateExpStats(){
        TYear y = listner.year;
        double[] values = {
                y == null ? 0 : y.getMeanNecExpenses(),
                y == null ? 0 : y.getMeanUnnecExpenses(),
                y == null ? 0 : y.getMeanExtraExpenses()
        };
        eTranSubType[] types = {eTranSubType.NECESSARY, eTranSubType.UNNECESSARY, eTranSubType.EXTRA};
        String[] names = {"Necessarie", "Superflue", "Extra"};
        int[] colors = {R.color.ew_cat_necessaria, R.color.ew_cat_superflua, R.color.ew_cat_extra};

        double total = 0;
        for (double v : values) total += Math.max(0, v);

        // Segmented bar, one part per category, 3dp apart
        LinearLayout bar = view.findViewById(R.id.expShareBar);
        bar.removeAllViews();
        bar.setClipToOutline(true);
        int gap = dp(3);
        boolean first = true;
        for (int i = 0; i < values.length; i++) {
            if (values[i] <= 0) continue;
            View part = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,
                    (float) (values[i] / total));
            if (!first) lp.setMarginStart(gap);
            part.setLayoutParams(lp);
            part.setBackgroundColor(color(colors[i]));
            bar.addView(part);
            first = false;
        }
        bar.setVisibility(total > 0 ? View.VISIBLE : View.GONE);

        // Rows: same look as the Movimenti list (category icon, name + share, amount)
        LinearLayout rows = view.findViewById(R.id.expMeanRows);
        rows.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < values.length; i++) {
            View row = inflater.inflate(R.layout.movement_row, rows, false);
            row.setClickable(false);
            row.setBackground(null);

            CategoryStyle.of(types[i]).applyTo(row.findViewById(R.id.rowIcon));
            ((TextView) row.findViewById(R.id.rowCategory)).setText(names[i]);

            TextView share = row.findViewById(R.id.rowDescription);
            int pct = total <= 0 ? 0 : (int) Math.round(Math.max(0, values[i]) * 100 / total);
            share.setText(pct + "% delle spese");
            share.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            share.setVisibility(View.VISIBLE);

            TextView amount = row.findViewById(R.id.rowAmount);
            amount.setText(Tools.formatEuro(values[i]));
            amount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

            rows.addView(row);
        }
    }

    private int dp(int v){
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @ColorInt
    private int color(@ColorRes int res){
        return ContextCompat.getColor(requireContext(), res);
    }
}
