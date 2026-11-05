package com.example.provajava.gui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.gui.activity.StatisticsActivityPage;
import com.example.provajava.gui.bottomsheet.BottomSheetYear;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Statistiche · Andamento: monthly balance (bars) and cumulative balance (line) of the selected year
public class StatisticsFragment extends Fragment implements iFragmentManaged {

    private static final String[] MONTH_INITIALS = {"G", "F", "M", "A", "M", "G", "L", "A", "S", "O", "N", "D"};

    private StatisticsActivityPage listner;
    private View view;
    private List<TMonth> months = new ArrayList<>();

    // Monthly and cumulative balance by month number (1..12); null where the month has no data
    private final Double[] balance = new Double[13];
    private final Double[] cumulative = new Double[13];
    private int lastMonth = 0;

    @Override
    public void onFragmentDismissed() {}

    // Called by the year picker and by the ‹ › arrows of the page header
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onFragmentClick(long id) {
        populateYear(id);
        populateGraphs();

        if(listner!=null){
            listner.onChangeYear(id);
        }
    }

    @Override
    public void onFragmentNewTransaction(TTransaction tr) {}

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.statistics, container, false);

        populateYear(-1);
        populateGraphs();

        return view;
    }

    public void openYearPicker(){
        BottomSheetYear bottomSheet = new BottomSheetYear(this, listner.dba);
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    public void setListner(StatisticsActivityPage listner) {
        this.listner = listner;
    }

    // ------------------------------------------------------------------------------------- data

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void populateYear(long id){

        TYear y = id <= 0 ? listner.year : listner.dba.getYearById(id);
        months = y == null ? new ArrayList<>() : listner.dba.getMonthsByYearId(y.getId());
        if (months == null) months = new ArrayList<>();

        for (int i = 0; i < 13; i++) { balance[i] = null; cumulative[i] = null; }
        lastMonth = 0;
        double sum = 0;
        for (TMonth t : months) {
            int m = t.getMonth();
            sum += t.getBalance();
            balance[m] = t.getBalance();
            cumulative[m] = sum;
            lastMonth = Math.max(lastMonth, m);
        }
    }

    private void populateGraphs(){
        setBalanceChart();
        setCumulativeChart();
        showBalance(lastMonth);
        showCumulative(lastMonth);
    }

    // ----------------------------------------------------------------------------- bar chart

    private void setBalanceChart(){
        BarChart chart = view.findViewById(R.id.balanceChart);

        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            if (balance[m] == null) continue;
            entries.add(new BarEntry(m, balance[m].floatValue()));
            colors.add(color(balance[m] >= 0 ? R.color.ew_chart_positive : R.color.ew_chart_negative));
        }

        styleChart(chart);
        if (entries.isEmpty()) { chart.clear(); return; }

        BarDataSet set = new BarDataSet(entries, "Bilancio");
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setColors(colors);
        set.setDrawValues(false);
        set.setHighLightColor(color(R.color.ew_text));
        set.setHighLightAlpha(40);

        BarData data = new BarData(set);
        data.setBarWidth(0.5f);
        chart.setData(data);
        fitYAxis(chart);

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override public void onValueSelected(Entry e, Highlight h) { showBalance((int) e.getX()); }
            @Override public void onNothingSelected() { showBalance(lastMonth); }
        });
        chart.invalidate();
    }

    private void showBalance(int month){
        TextView label = view.findViewById(R.id.balMonthLabel);
        TextView value = view.findViewById(R.id.balMonthValue);
        if (month <= 0 || balance[month] == null) {
            label.setText("");
            value.setText("Nessun dato");
            value.setTextColor(color(R.color.ew_text_secondary));
            return;
        }
        double b = balance[month];
        label.setText(monthName(month));
        value.setText(Tools.formatEuro(b, true));
        value.setTextColor(color(b >= 0 ? R.color.ew_income : R.color.ew_expense));
    }

    // ---------------------------------------------------------------------------- line chart

    private void setCumulativeChart(){
        LineChart chart = view.findViewById(R.id.cumulativeChart);
        @ColorInt int primary = color(R.color.ew_primary);

        List<Entry> entries = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            if (cumulative[m] != null) entries.add(new Entry(m, cumulative[m].floatValue()));
        }

        styleChart(chart);
        if (entries.isEmpty()) { chart.clear(); return; }

        LineDataSet line = new LineDataSet(entries, "Saldo");
        line.setAxisDependency(YAxis.AxisDependency.RIGHT);
        line.setColor(primary);
        line.setLineWidth(2.5f);
        line.setDrawCircles(false);
        line.setDrawValues(false);
        line.setDrawFilled(true);
        line.setFillColor(primary);
        line.setFillAlpha(26);
        line.setHighLightColor(color(R.color.ew_outline));
        line.setDrawHorizontalHighlightIndicator(false);
        line.setHighlightLineWidth(1f);

        LineData data = new LineData(line);

        // Last point marked with a white dot outlined in the primary color, as in the design
        if (!entries.isEmpty()) {
            List<Entry> last = new ArrayList<>();
            last.add(entries.get(entries.size() - 1));
            LineDataSet dot = new LineDataSet(last, "Ultimo");
            dot.setAxisDependency(YAxis.AxisDependency.RIGHT);
            dot.setDrawValues(false);
            dot.setCircleColor(primary);
            dot.setCircleRadius(5f);
            dot.setCircleHoleColor(Color.WHITE);
            dot.setCircleHoleRadius(3f);
            dot.setHighlightEnabled(false);
            dot.setColor(Color.TRANSPARENT);
            data.addDataSet(dot);
        }

        chart.setData(data);
        fitYAxis(chart);

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override public void onValueSelected(Entry e, Highlight h) { showCumulative((int) e.getX()); }
            @Override public void onNothingSelected() { showCumulative(lastMonth); }
        });
        chart.invalidate();
    }

    private void showCumulative(int month){
        TextView label = view.findViewById(R.id.cumMonthLabel);
        TextView value = view.findViewById(R.id.cumMonthValue);
        if (month <= 0 || cumulative[month] == null) {
            label.setText("");
            value.setText("Nessun dato");
            value.setTextColor(color(R.color.ew_text_secondary));
            return;
        }
        double c = cumulative[month];
        label.setText("A " + monthName(month).toLowerCase(Locale.ITALIAN));
        value.setText(Tools.formatEuro(c, true));
        value.setTextColor(color(c >= 0 ? R.color.ew_income : R.color.ew_expense));
    }

    // ------------------------------------------------------------------------------- shared

    // Common look of both charts: no zoom/drag (so the pager can still swipe), month initials
    // on the x axis, a few light dashed lines and compact amounts on the right
    private void styleChart(BarLineChartBase<?> chart){
        Typeface font = Typeface.create(ResourcesCompat.getFont(requireContext(), R.font.nunito), Typeface.BOLD);
        int secondary = color(R.color.ew_text_secondary);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDragEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setNoDataText("Nessun dato per quest'anno");
        chart.setNoDataTextColor(secondary);
        chart.setNoDataTextTypeface(font);
        chart.setExtraBottomOffset(4f);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setAxisMinimum(0.5f);
        x.setAxisMaximum(12.5f);
        x.setGranularity(1f);
        x.setLabelCount(12);
        x.setDrawGridLines(false);
        x.setDrawAxisLine(false);
        x.setTypeface(font);
        x.setTextSize(12f);
        x.setTextColor(secondary);
        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                int m = Math.round(value);
                return m >= 1 && m <= 12 ? MONTH_INITIALS[m - 1] : "";
            }
        });

        chart.getAxisLeft().setEnabled(false);

        YAxis y = chart.getAxisRight();
        y.setEnabled(true);
        y.resetAxisMinimum();
        y.resetAxisMaximum();
        y.setLabelCount(3, false);
        y.setDrawAxisLine(false);
        y.setGridColor(color(R.color.ew_divider));
        y.enableGridDashedLine(6f, 6f, 0f);
        y.setDrawZeroLine(true);
        y.setZeroLineColor(color(R.color.ew_outline));
        y.setZeroLineWidth(1f);
        y.setTypeface(font);
        y.setTextSize(11f);
        y.setTextColor(secondary);
        y.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                return compact(value);
            }
        });
    }

    // Y range always includes zero (bars start from the zero line), with a little space around the data
    private static void fitYAxis(BarLineChartBase<?> chart){
        float min = Math.min(0f, chart.getData().getYMin());
        float max = Math.max(0f, chart.getData().getYMax());
        float pad = Math.max((max - min) * 0.1f, 1f);
        YAxis y = chart.getAxisRight();
        y.setAxisMinimum(min < 0 ? min - pad : 0f);
        y.setAxisMaximum(max + pad);
    }

    // 500 → "500", 1500 → "1,5k", -2000 → "−2k"
    private static String compact(float v){
        String sign = v < 0 ? "−" : "";
        float a = Math.abs(v);
        if (a >= 1000) {
            float k = Math.round(a / 100f) / 10f;
            String s = (k == (int) k) ? String.valueOf((int) k) : String.valueOf(k).replace('.', ',');
            return sign + s + "k";
        }
        return sign + Math.round(a);
    }

    private static String monthName(int month){
        String n = Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ITALIAN);
        return n.substring(0, 1).toUpperCase(Locale.ITALIAN) + n.substring(1);
    }

    @ColorInt
    private int color(int res){
        return ContextCompat.getColor(requireContext(), res);
    }
}
