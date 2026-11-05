package com.example.provajava.gui.activity;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.provajava.R;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.BottomNav;
import com.example.provajava.gui.PillTabs;
import com.example.provajava.gui.fragment.TodayFragment;
import com.example.provajava.gui.fragment.TransactionListFragment;
import com.example.provajava.gui.fragment.TransactionSearchFragment;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class TodayActivityPage extends AppCompatActivity implements iActivityManaged{

    // How many months the ‹ › arrows look ahead/back for a month with data
    private static final int MAX_MONTH_GAP = 24;

    private TransactionListFragment listFragment;
    private TodayFragment todayFragment;
    private TransactionSearchFragment searchFragment;
    private DatabaseAccess dba;
    private TMonth month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todaypage);

        ViewPager2 viewPager = findViewById(R.id.todayViewPage);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        dba = new DatabaseAccess(getApplicationContext());
        month = dba.getMonth(LocalDate.now().getMonthValue(), LocalDate.now().getYear());

        manageButtons();
        manageHeader();
        manageTabs(viewPager);
        updateHeader();
    }

    public TodayFragment getTodayFragment(){
        return todayFragment;
    }

    private void manageButtons(){
        BottomNav.setup(this, R.id.nav_home);
    }

    // Header: ‹ › move to the previous/next month that has data, the title opens the month picker
    private void manageHeader(){
        findViewById(R.id.periodPrevBtn).setContentDescription("Mese precedente");
        findViewById(R.id.periodNextBtn).setContentDescription("Mese successivo");
        findViewById(R.id.periodPrevBtn).setOnClickListener(v -> shiftMonth(-1));
        findViewById(R.id.periodNextBtn).setOnClickListener(v -> shiftMonth(1));
        findViewById(R.id.periodTitleBox).setOnClickListener(v -> {
            if(todayFragment != null) todayFragment.openMonthPicker();
        });
    }

    private void updateHeader(){
        LocalDate shown = shownMonthDate();
        TextView title = findViewById(R.id.periodTitle);
        TextView year = findViewById(R.id.periodLabel);
        String name = Month.of(shown.getMonthValue()).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ITALIAN);
        title.setText(name.substring(0, 1).toUpperCase(Locale.ITALIAN) + name.substring(1));
        year.setText(String.valueOf(shown.getYear()));
    }

    // First day of the month currently shown (today's month if none is loaded yet)
    private LocalDate shownMonthDate(){
        if(month == null){
            return LocalDate.now().withDayOfMonth(1);
        }
        return LocalDate.of(dba.getYearById(month.getYearId()).getYear(), month.getMonth(), 1);
    }

    // Look for the nearest month with data in the given direction (months never used have no record)
    private void shiftMonth(int direction){
        if(todayFragment == null) return;
        LocalDate start = shownMonthDate();
        for(int i = 1; i <= MAX_MONTH_GAP; i++){
            LocalDate d = start.plusMonths((long) direction * i);
            TMonth m = dba.getMonth(d.getMonthValue(), d.getYear());
            if(m != null){
                todayFragment.onFragmentClick(m.getId());
                return;
            }
        }
        Toast.makeText(this, direction < 0 ? "Nessun mese precedente" : "Nessun mese successivo",
                Toast.LENGTH_SHORT).show();
    }

    // Tabs Riepilogo / Movimenti, kept in sync with the pager (tap or swipe)
    private void manageTabs(ViewPager2 viewPager){
        PillTabs.bind(viewPager, findViewById(R.id.tabSummary), findViewById(R.id.tabMovements));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onChangeMonth(TMonth m) {
        month = m;
        updateHeader();
        if(listFragment!=null){
            listFragment.updateMonth(month);
            if(listFragment.getView()!=null){
                listFragment.createOrUpdateView();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onNewTransaction(TTransaction tr) {
        if(listFragment!=null){
            listFragment.createOrUpdateView();
        }

        if(todayFragment!=null){
            todayFragment.updateView();
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 1) {
                listFragment = new TransactionListFragment();
                listFragment.setListner(TodayActivityPage.this);
                return listFragment;
            }else{
                todayFragment = new TodayFragment();
                todayFragment.setListner(TodayActivityPage.this);
                return todayFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

    }

    public TMonth getMonth(){
        return month;
    }

    public DatabaseAccess getDba(){
        return dba;
    }
}
