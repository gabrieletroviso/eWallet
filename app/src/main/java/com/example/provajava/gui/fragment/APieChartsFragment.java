package com.example.provajava.gui.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.gui.activity.StatisticsActivityPage;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

// Statistiche · Spese / Entrate: donut chart of the year's categories.
// The selected category (the largest by default) is shown in the middle of the ring and highlighted
// in the legend below; tapping a slice or a legend row selects it.
public abstract class APieChartsFragment extends Fragment implements iFragmentManaged {

    // One category of the chart
    protected static final class Slice {
        final String label;
        final float value;
        @ColorRes final int color;      // slice, legend dot and bar
        @ColorRes final int accent;     // percentage text (darker, for contrast)
        @ColorRes final int container;  // background of the selected legend row

        protected Slice(String label, float value, @ColorRes int color, @ColorRes int accent, @ColorRes int container) {
            this.label = label;
            this.value = value;
            this.color = color;
            this.accent = accent;
            this.container = container;
        }
    }

    protected StatisticsActivityPage listner;
    protected View view;

    private final List<Slice> slices = new ArrayList<>();
    private float total = 0;
    private int selected = -1;

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
        view = inflater.inflate(R.layout.statisticspie, container, false);
        ((TextView) view.findViewById(R.id.pieHead)).setText(getTitle());
        populate();
        return view;
    }

    public void setListner(StatisticsActivityPage listner) {
        this.listner = listner;
    }

    // Called by the page when the year changes
    public View onChangeYear(long id){
        if (getView() != null) {
            populate();
        }
        return getView();
    }

    // ----------------------------------------------------------------------------- subclasses

    abstract protected String getTitle();

    // Categories of the current year (listner.year), zero values included: they are skipped here
    abstract protected List<Slice> getSlices();

    // ------------------------------------------------------------------------------- content

    private void populate(){
        slices.clear();
        total = 0;
        if (listner.year != null) {
            for (Slice s : getSlices()) {
                if (s.value > 0) {
                    slices.add(s);
                    total += s.value;
                }
            }
        }

        // Largest category selected by default
        selected = -1;
        for (int i = 0; i < slices.size(); i++) {
            if (selected < 0 || slices.get(i).value > slices.get(selected).value) selected = i;
        }

        setChart();
        buildLegend();
        showSelected();
    }

    private void setChart(){
        PieChart chart = view.findViewById(R.id.pieChart);
        Typeface font = ResourcesCompat.getFont(requireContext(), R.font.nunito);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.setUsePercentValues(false);
        chart.setRotationEnabled(false);
        chart.setRotationAngle(270f);           // first slice starts at the top
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setHoleRadius(73f);
        chart.setTransparentCircleRadius(0f);
        chart.setDrawCenterText(false);         // the center is drawn by our own TextViews
        chart.setNoDataText("Nessun dato per quest'anno");
        chart.setNoDataTextColor(color(R.color.ew_text_secondary));
        chart.setNoDataTextTypeface(font);
        chart.setExtraOffsets(0f, 0f, 0f, 0f);

        if (slices.isEmpty()) {
            chart.clear();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Slice s : slices) {
            entries.add(new PieEntry(s.value, s.label));
            colors.add(color(s.color));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(colors);
        set.setSliceSpace(2f);
        set.setSelectionShift(4f);
        set.setDrawValues(false);

        chart.setData(new PieData(set));
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                select((int) h.getX(), false);
            }

            @Override
            public void onNothingSelected() {
                // Keep the current selection: tapping the hole should not clear the center
                highlightSelected();
            }
        });
        highlightSelected();
        chart.invalidate();
    }

    private void buildLegend(){
        LinearLayout rows = view.findViewById(R.id.legendRows);
        rows.removeAllViews();
        rows.setVisibility(slices.isEmpty() ? View.GONE : View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < slices.size(); i++) {
            Slice s = slices.get(i);
            int pct = percent(s.value);
            View row = inflater.inflate(R.layout.pie_legend_row, rows, false);

            ViewCompat.setBackgroundTintList(row.findViewById(R.id.legendDot), ColorStateList.valueOf(color(s.color)));
            ((TextView) row.findViewById(R.id.legendName)).setText(s.label);
            ((TextView) row.findViewById(R.id.legendPercent)).setText(pct + "%");

            View meter = row.findViewById(R.id.legendMeter);
            ViewCompat.setBackgroundTintList(meter, ColorStateList.valueOf(color(s.color)));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) meter.getLayoutParams();
            lp.weight = pct;
            meter.setLayoutParams(lp);

            final int index = i;
            row.setOnClickListener(v -> select(index, true));
            rows.addView(row);
        }
    }

    private void select(int index, boolean fromLegend){
        if (index < 0 || index >= slices.size()) return;
        selected = index;
        if (fromLegend) highlightSelected();
        showSelected();
    }

    private void highlightSelected(){
        PieChart chart = view.findViewById(R.id.pieChart);
        if (selected >= 0 && chart.getData() != null) {
            chart.highlightValue(selected, 0, false);
        }
    }

    // Center of the ring + selected legend row
    private void showSelected(){
        TextView label = view.findViewById(R.id.centerLabel);
        TextView value = view.findViewById(R.id.centerValue);
        TextView pct = view.findViewById(R.id.centerPercent);

        if (selected < 0) {
            label.setText("");
            value.setText("");
            pct.setText("");
            return;
        }

        Slice s = slices.get(selected);
        label.setText(s.label);
        value.setText(Tools.formatEuro(s.value));
        pct.setText(percent(s.value) + "%");
        pct.setTextColor(color(s.accent));

        Typeface bold = Typeface.create(ResourcesCompat.getFont(requireContext(), R.font.nunito), Typeface.BOLD);
        Typeface extraBold = ResourcesCompat.getFont(requireContext(), R.font.nunito_extrabold);
        LinearLayout rows = view.findViewById(R.id.legendRows);
        for (int i = 0; i < rows.getChildCount(); i++) {
            View row = rows.getChildAt(i);
            boolean on = i == selected;
            row.setBackgroundColor(on ? color(slices.get(i).container) : Color.TRANSPARENT);
            ((TextView) row.findViewById(R.id.legendName))
                    .setTypeface(on ? extraBold : bold);
        }
    }

    private int percent(float v){
        return total <= 0 ? 0 : Math.round(v * 100f / total);
    }

    @ColorInt
    protected int color(@ColorRes int res){
        return ContextCompat.getColor(requireContext(), res);
    }
}
