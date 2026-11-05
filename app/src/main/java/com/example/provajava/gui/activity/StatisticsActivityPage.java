package com.example.provajava.gui.activity;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.provajava.R;
import com.example.provajava.gui.Notice;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.BottomNav;
import com.example.provajava.gui.PillTabs;
import com.example.provajava.gui.fragment.ExpensesFragment;
import com.example.provajava.gui.fragment.IncomeFragment;
import com.example.provajava.gui.fragment.StatisticsDataFragment;
import com.example.provajava.gui.fragment.StatisticsFragment;
import com.example.provajava.gui.fragment.TransactionSearchFragment;

import java.time.LocalDate;

public class StatisticsActivityPage extends AppCompatActivity implements iActivityManaged{

    // How many years the ‹ › arrows look back/ahead for a year with data
    private static final int MAX_YEAR_GAP = 10;

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
        manageHeader();
        PillTabs.bind(viewPager, findViewById(R.id.tabTrend), findViewById(R.id.tabExpenses),
                findViewById(R.id.tabIncome), findViewById(R.id.tabAverages));
        updateHeader();
    }

    private void manageButtons(){
        BottomNav.setup(this, R.id.nav_stats);
    }

    // Header: ‹ › move to the previous/next year that has data, the title opens the year picker
    private void manageHeader(){
        findViewById(R.id.periodPrevBtn).setContentDescription("Anno precedente");
        findViewById(R.id.periodNextBtn).setContentDescription("Anno successivo");
        findViewById(R.id.periodPrevBtn).setOnClickListener(v -> shiftYear(-1));
        findViewById(R.id.periodNextBtn).setOnClickListener(v -> shiftYear(1));
        findViewById(R.id.periodTitleBox).setOnClickListener(v -> {
            if(statisticsFragment != null) statisticsFragment.openYearPicker();
        });
    }

    private void updateHeader(){
        ((TextView) findViewById(R.id.periodLabel)).setText("Statistiche");
        ((TextView) findViewById(R.id.periodTitle)).setText(
                String.valueOf(year != null ? year.getYear() : LocalDate.now().getYear()));
    }

    private void shiftYear(int direction){
        if(statisticsFragment == null || year == null) return;
        for(int i = 1; i <= MAX_YEAR_GAP; i++){
            TYear y = dba.getYear(year.getYear() + direction * i);
            if(y != null){
                statisticsFragment.onFragmentClick(y.getId());
                return;
            }
        }
        Notice.info(this, direction < 0 ? "Nessun anno precedente con movimenti" : "Nessun anno successivo con movimenti");
    }

    @Override
    public void onChangeMonth(TMonth m) {
    }

    @Override
    public void onNewTransaction(TTransaction tr) {
    }

    public void onChangeYear(long yId){

        year = dba.getYearById(yId);
        updateHeader();

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
