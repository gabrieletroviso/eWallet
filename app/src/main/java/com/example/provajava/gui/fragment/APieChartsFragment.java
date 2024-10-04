package com.example.provajava.gui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.gui.activity.StatisticsActivityPage;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public abstract class APieChartsFragment extends Fragment implements iFragmentManaged {

    protected StatisticsActivityPage listner;
    protected View view;
    protected int pieId;
    protected ArrayList<Integer> colorList;

    @Override
    public void onFragmentDismissed() {

    }

    @Override
    public void onFragmentClick(long id) {

    }

    @Override
    public void onFragmentNewTransaction(TTransaction tr) {

    }

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.statisticspie, container, false);
        ConstraintLayout firstConst = view.findViewById(R.id.yearStatsConst);

        setColorList();
        setHeader();

        PieChart pieChart = createPieChart();
        pieChart = setData(pieChart);
        setPieChartLoc(pieChart, firstConst);

        return view;
    }

    public void setListner(StatisticsActivityPage listner) {
        this.listner = listner;
    }

    public View onChangeYear(long id){

        View view = getView();
        ConstraintLayout firstConst = view.findViewById(R.id.yearStatsConst);
        firstConst.removeView(view.findViewById(pieId));
        PieChart pieChart = createPieChart();
        pieChart = setData(pieChart);
        setPieChartLoc(pieChart, firstConst);
        return view;
    }

    protected PieChart definePieChartProperties(PieChart piechart, ArrayList<PieEntry> entries){

        PieChart managedPie = piechart;

        PieData pieData = createPieData(entries);
        managedPie.setData(pieData);
        managedPie = setLegend(managedPie);

        return managedPie;
    }

    private PieChart setLegend(PieChart pieChart){

        PieChart managedPie = pieChart;

        Legend legend = managedPie.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
        legend.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.nunito));
        legend.setTextSize(16f);
        legend.setTextColor(getResources().getColor(R.color.apptext, null));

        return managedPie;
    }

    private PieData createPieData(ArrayList<PieEntry> entries){

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colorList.subList(0, entries.size()));
        dataSet.setSliceSpace(6f);
        dataSet.setSelectionShift(5f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(24f);
        pieData.setValueTypeface(ResourcesCompat.getFont(view.getContext(), R.font.nunito));
        pieData.setValueTextColor(R.color.apptext);

        return pieData;
    }

    private PieChart createPieChart(){

        pieId = View.generateViewId();
        PieChart pieChart = new PieChart(view.getContext());
        pieChart.setId(pieId);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);

        TextView y = view.findViewById(R.id.pieYearHead);
        y.setText(String.valueOf(listner.year.getYear()));

        return pieChart;

    }

    private ConstraintLayout setPieChartLoc(PieChart pie, ConstraintLayout c){

        ConstraintLayout clyt = c;
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        pie.setLayoutParams(params);
        clyt.addView(pie);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(clyt);

        constraintSet.connect(pie.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
        constraintSet.connect(pie.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
        constraintSet.connect(pie.getId(), ConstraintSet.TOP, R.id.pieYearHead, ConstraintSet.TOP, 200
        );
        constraintSet.connect(pie.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);


        constraintSet.applyTo(clyt);
        return clyt;

    }

    abstract protected void setHeader();

    abstract protected PieChart setData(PieChart pie);

    abstract protected void setColorList();

}
