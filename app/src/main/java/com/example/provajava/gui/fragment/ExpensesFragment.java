package com.example.provajava.gui.fragment;

import android.widget.TextView;

import com.example.provajava.R;
import com.example.provajava.enumerator.eTranSubType;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class ExpensesFragment extends APieChartsFragment {

    protected void setHeader(){
        TextView header = view.findViewById(R.id.pieHead);
        header.setText("Statistiche spese");
    }

    protected PieChart setData(PieChart pie) {

        PieChart managedPie = pie;

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        float nec = (float)listner.year.getNecExpenses();
        float unn = (float)listner.year.getUnnecExpenses();
        float ext = (float)listner.year.getExtraExpenses();

        if(nec!=0.f){
            pieEntries.add(new PieEntry(nec, eTranSubType.NECESSARY.toString()));
        }
        if(unn!=0.f){
            pieEntries.add(new PieEntry(unn, eTranSubType.UNNECESSARY.toString()));
        }
        if(ext!=0.f){
            pieEntries.add(new PieEntry(ext, eTranSubType.EXTRA.toString()));
        }

        managedPie = definePieChartProperties(managedPie, pieEntries);

        return managedPie;
    }

    protected void setColorList(){
        colorList = new ArrayList<>();
        colorList.add(getResources().getColor(R.color.pastelblu, null));
        colorList.add(getResources().getColor(R.color.pastelgreen, null));
        colorList.add(getResources().getColor(R.color.pastelpink, null));
    }
}


