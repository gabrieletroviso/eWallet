package com.example.provajava.gui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.provajava.R;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.fragment.ExpensesFragment;
import com.example.provajava.gui.fragment.IncomeFragment;
import com.example.provajava.gui.fragment.StatisticsDataFragment;
import com.example.provajava.gui.fragment.StatisticsFragment;

import java.time.LocalDate;

public class StatisticsActivityPage extends AppCompatActivity implements iActivityManaged{

    private StatisticsFragment statisticsFragment;
    private ExpensesFragment expFragment;
    private IncomeFragment incomeFragment;
    private StatisticsDataFragment dataFragment;
    public DatabaseAccess dba;
    public TYear year;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statisticspage);

        ViewPager2 viewPager = findViewById(R.id.statisticsViewPage);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        dba = new DatabaseAccess(getApplicationContext());
        year = dba.getYear(LocalDate.now().getYear());

        manageButtons();
    }

    private void manageButtons(){

        ImageButton today = findViewById(R.id.statisticsTodBtn);
        ImageButton pref = findViewById(R.id.statisticsPrefBtn);

        today.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivityPage.this, TodayActivityPage.class);
            startActivity(intent);
        });

        pref.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivityPage.this, ToolActivityPage.class);
            startActivity(intent);
        });

    }

    @Override
    public void onChangeMonth(TMonth m) {
    }

    @Override
    public void onNewTransaction(TTransaction tr) {
    }

    public void onChangeYear(long yId){

        if(year!=null){
            year = dba.getYearById(yId);
        }

        if(expFragment !=null && expFragment.getView()!=null){
            expFragment.onChangeYear(yId);
        }

        if(incomeFragment !=null && incomeFragment.getView()!=null){
            incomeFragment.onChangeYear(yId);
        }

        if(dataFragment !=null && dataFragment.getView()!=null){
            dataFragment.onChangeYear();
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 1) {
                expFragment = new ExpensesFragment();
                expFragment.setListner(StatisticsActivityPage.this);
                return expFragment;
            } else if (position == 2) {
                incomeFragment = new IncomeFragment();
                incomeFragment.setListner(StatisticsActivityPage.this);
                return incomeFragment;
            } else if (position == 3){
                dataFragment = new StatisticsDataFragment();
                dataFragment.setListner(StatisticsActivityPage.this);
                return dataFragment;
            }else{
                statisticsFragment = new StatisticsFragment();
                statisticsFragment.setListner(StatisticsActivityPage.this);
                return statisticsFragment;
            }
        }


        @Override
        public int getItemCount() {
            return 4;
        }

    }
}
