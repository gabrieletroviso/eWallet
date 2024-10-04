package com.example.provajava.gui.fragment;

import android.widget.TextView;

import com.example.provajava.R;
import com.example.provajava.enumerator.eTranSubType;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class IncomeFragment extends APieChartsFragment{

    protected void setHeader(){
        TextView header = view.findViewById(R.id.pieHead);
        header.setText("Statistiche entrate");
    }

    protected PieChart setData(PieChart pie){

        PieChart managedPie = pie;

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        float sal = (float) listner.dba.getAnnualSalaryIncome(listner.year.getId());
        float oth = (float) listner.dba.getAnnualOtherIncome(listner.year.getId());

        if (sal != 0.f) {
            pieEntries.add(new PieEntry(sal, eTranSubType.SALARY.toString()));
        }
        if (oth != 0.f) {
            pieEntries.add(new PieEntry(oth, eTranSubType.OTHER_INCOME.toString()));
        }

        managedPie = definePieChartProperties(managedPie, pieEntries);

        return managedPie;
    }

    protected void setColorList(){
        colorList = new ArrayList<>();
        colorList.add(getResources().getColor(R.color.pastelsalary, null));
        colorList.add(getResources().getColor(R.color.pastelgreen, null));
    }

}
