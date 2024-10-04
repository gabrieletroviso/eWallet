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
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.gui.activity.StatisticsActivityPage;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

public class StatisticsDataFragment extends Fragment implements iFragmentManaged {

    private StatisticsActivityPage listner;
    private View view;
    private TYear year;

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

        view = inflater.inflate(R.layout.statisticsdata, container, false);

        populateGlobalStats();
        populateExpStats();

        return view;
    }

    public void onChangeYear(){
        populateGlobalStats();
        populateExpStats();
    }

    public void setListner(StatisticsActivityPage listner) {
        this.listner = listner;
    }

    private void populateGlobalStats(){

        TextView header = view.findViewById(R.id.dataYearHead);
        header.setText(String.valueOf(listner.year.getYear()));

        TextView lbl = view.findViewById(R.id.globalLbl);
        lbl.setText("Medie mensili globali");

        TextView mInc = view.findViewById(R.id.incMeanVal);
        TextView mExp = view.findViewById(R.id.expMeanVal);
        TextView mBil = view.findViewById(R.id.balMeanVal);
        TextView proj = view.findViewById(R.id.projVal);

        double bal = listner.year.getMeanBalance();
        double projval = listner.year.getBalanceProjection();

        mInc.setText(Tools.roundToPrint(listner.year.getMeanIncome()));
        mExp.setText(Tools.roundToPrint(listner.year.getMeanExpenses()));
        mBil.setTextColor(getResources().getColor(bal>0 ? R.color.dark_green : R.color.dark_red, null));
        mBil.setText(Tools.roundToPrint(bal));
        proj.setTextColor(getResources().getColor(projval>0 ? R.color.dark_green : R.color.dark_red, null));
        proj.setText(Tools.roundToPrint(projval));

    }

    private void populateExpStats(){

        TextView lbl = view.findViewById(R.id.expMeanLb);
        lbl.setText("Spese medie mensili");

        TextView expNec = view.findViewById(R.id.meanExpNecVal);
        TextView expUnn = view.findViewById(R.id.meanExpUnnVal);
        TextView expExt = view.findViewById(R.id.meanExpExtraVal);

        expNec.setText(Tools.roundToPrint(listner.year.getMeanNecExpenses()));
        expUnn.setText(Tools.roundToPrint(listner.year.getMeanUnnecExpenses()));
        expExt.setText(Tools.roundToPrint(listner.year.getMeanExtraExpenses()));

    }
}
