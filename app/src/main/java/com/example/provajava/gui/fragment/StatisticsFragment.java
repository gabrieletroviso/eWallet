package com.example.provajava.gui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointD;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment implements iFragmentManaged {

    private StatisticsActivityPage listner;
    private View view;
    private List<TMonth> months;

    @Override
    public void onFragmentDismissed() {}

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
    public void onFragmentNewTransaction(TTransaction tr) {

    }

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.statistics, container, false);

        populateYear();
        populateGraphs();

        return view;

    }

    private void populateGraphs(){
        setYearSavingsGraph();
        setYearBehaviourGraph();
    }

    // Set saving graph
    private void setYearSavingsGraph(){
        ConstraintLayout savingCont = view.findViewById(R.id.savCont);
        savingCont.removeAllViews();

        ScatterChart sChart = new ScatterChart(view.getContext());

        ArrayList<Entry> cumBalPos = new ArrayList<>();
        ArrayList<Entry> cumBalNeg = new ArrayList<>();
        float prevBal = 0;

        for (TMonth t : months) {

            int m = t.getMonth();
            prevBal += (float)t.getBalance();

            if(prevBal>=0){
                cumBalPos.add(new Entry(m, (float) (prevBal)/1000));
            }else{
                cumBalNeg.add(new Entry(m, (float) (prevBal)/1000));
            }
        }

        sChart = managePlot(sChart, cumBalPos, cumBalNeg);
        savingCont.addView(sChart);

    }

    // Populate annual balance graphs
    private void setYearBehaviourGraph(){

        ConstraintLayout yearBeaCont = view.findViewById(R.id.yearBheavCont);
        yearBeaCont.removeAllViews();

        ScatterChart sChart = new ScatterChart(view.getContext());

        ArrayList<Entry> balPos = new ArrayList<>();
        ArrayList<Entry> balNeg = new ArrayList<>();

        for (TMonth t : months) {

            int m = t.getMonth();
            double bal = t.getBalance();

            if(bal>=0){
                balPos.add(new Entry(m, (float) bal/1000));
            }else{
                balNeg.add(new Entry(m, (float) bal/1000));
            }

        }

        sChart = managePlot(sChart, balPos, balNeg);
        yearBeaCont.addView(sChart);
    }

    // Manage scatter plot with specific data and properties
    private ScatterChart managePlot(ScatterChart sChart, ArrayList pos, ArrayList neg){

        ScatterChart managed = sChart;

        sChart = setScatterData(sChart, pos, neg);
        sChart = manageXAxis(sChart);
        sChart = manageYAxis(sChart);
        sChart = setScatterParams(sChart);

        sChart.getDescription().setEnabled(false);
        sChart.invalidate();

        return managed;

    }

    private ScatterChart manageYAxis(ScatterChart sChart){

        ScatterChart manScatt = sChart;

        float min = getMinValues(sChart.getData())-0.5f;
        float max = getMaxValue(sChart.getData())+0.5f;
        float del = max-min;

        YAxis yAxisLeft = manScatt.getAxisLeft();
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisLeft.setAxisMinimum(min);
        yAxisLeft.setAxisMaximum(max);
        yAxisLeft.setGranularity(0.5f);
        yAxisLeft.setLabelCount(Math.round(del/0.5f));
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat f = new DecimalFormat("##.00");
                return f.format(value) + " k";
            }
        });

        yAxisLeft.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.nunito));
        yAxisLeft.setTextSize(10f);

        YAxis yAxisRight = manScatt.getAxisRight();
        yAxisRight.setEnabled(false);

        return manScatt;
    }

    private ScatterChart manageXAxis(ScatterChart sChart){

        ScatterChart manScatt = sChart;

        XAxis xAxis = sChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int month = (int) value;
                switch (month) {
                    case 1: return "Gen";
                    case 2: return "Feb";
                    case 3: return "Mar";
                    case 4: return "Apr";
                    case 5: return "Mag";
                    case 6: return "Giu";
                    case 7: return "Lug";
                    case 8: return "Ago";
                    case 9: return "Set";
                    case 10: return "Ott";
                    case 11: return "Nov";
                    case 12: return "Dic";
                    default: return "";
                }
            }
        });
        xAxis.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.nunito));
        xAxis.setLabelRotationAngle(-45);

        return manScatt;

    }

    // Convert to pixel
    private int convertDpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private float getMaxValue(ScatterData data){

        List<IScatterDataSet> dataSets = data.getDataSets();
        ScatterDataSet sdata = (ScatterDataSet) dataSets.get(0);
        List<Entry> list = sdata.getValues();

        if(!list.isEmpty()){
            float ret = list.get(0).getY();
            for (Entry e : list) {
                if(ret<e.getY()){
                    ret = e.getY();
                }
            }
            return ret;
        }else{
            return 0;
        }
    }

    private float getMinValues(ScatterData data){

        List<IScatterDataSet> dataSets = data.getDataSets();
        ScatterDataSet sdata = (ScatterDataSet) dataSets.get(1);
        List<Entry> list = sdata.getValues();

        if(!list.isEmpty()){
            float ret = list.get(0).getY();
            for (Entry e : list) {
                if(ret>e.getY()){
                    ret = e.getY();
                }
            }
            return ret;
        }else{
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void populateYear(){
        populateYear(-1);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void populateYear(long id){

        TYear y;
        Button yearBtn = view.findViewById(R.id.yearBtn);
        if(id<=0){
            y = listner.year;
        }else{
            y = listner.dba.getYearById(id);
        }

        months = listner.dba.getMonthsByYearId(y.getId());
        yearBtn.setText(String.valueOf(y.getYear()));

        yearBtn.setOnClickListener(v -> {
            BottomSheetYear bottomSheet = new BottomSheetYear(this, listner.dba);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
    }

    private ScatterChart setScatterData(ScatterChart sCh, ArrayList pos, ArrayList neg){

        ScatterDataSet setBalNegative = new ScatterDataSet(neg, "Negative Balance");
        setBalNegative.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        setBalNegative.setColor(getResources().getColor(R.color.apptext, null));
        setBalNegative.setScatterShapeHoleColor(getResources().getColor(R.color.dark_red, null));
        setBalNegative.setScatterShapeHoleRadius(5f);
        setBalNegative.setScatterShapeSize(40f);
        setBalNegative.setDrawValues(false);

        ScatterDataSet setBalPositive = new ScatterDataSet(pos, "Positive Balance");
        setBalPositive.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        setBalPositive.setColor(getResources().getColor(R.color.apptext, null));  // Colore dei punti
        setBalPositive.setScatterShapeHoleColor(getResources().getColor(R.color.dark_green, null));  // Colore del buco
        setBalPositive.setScatterShapeHoleRadius(5f);
        setBalPositive.setScatterShapeSize(40f);
        setBalPositive.setDrawValues(false);

        ScatterData scatterData = new ScatterData();
        scatterData.addDataSet(setBalPositive);
        scatterData.addDataSet(setBalNegative);

        sCh.setData(scatterData);
        sCh.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int[] pos = getPositionOnScreen(sCh, setBalNegative, e);
                showPopupWindow(pos, sCh, e.getY());
            }

            @Override
            public void onNothingSelected() {}

        });

        return sCh;
    }

    private ScatterChart setScatterParams(ScatterChart sChart){

        ScatterChart managed = sChart;

        Legend legend = managed.getLegend();
        legend.setEnabled(false);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                convertDpToPx(330), convertDpToPx(210));

        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        managed.setLayoutParams(params);
        return managed;
    }

    // Show popup on clicked point
    private void showPopupWindow(int[] pos, ScatterChart scCh, float value) {

        LayoutInflater inflater = (LayoutInflater) listner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.graphpopup, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupText = popupView.findViewById(R.id.popup_text);
        popupText.setText("€"+ Tools.roundToPrint(value*1000));

        popupWindow.showAtLocation(scCh, Gravity.NO_GRAVITY, pos[0], pos[1]);
        popupView.setOnClickListener(v -> popupWindow.dismiss());
    }

    // Get position in pixels
    private int[] getPositionOnScreen(ScatterChart scCh, ScatterDataSet set, Entry entry){

        int[] chartLocation = new int[2];
        scCh.getLocationOnScreen(chartLocation);
        MPPointD point = scCh.getTransformer(set.getAxisDependency()).getPixelForValues(entry.getX(), entry.getY());

        int xPosition = (int) (chartLocation[0] + point.x);
        int yPosition = (int) (chartLocation[1] + point.y);

        return new int[]{xPosition, yPosition};
    }

    public void setListner(StatisticsActivityPage listner) {
        this.listner = listner;
    }
}
