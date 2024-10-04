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
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.fragment.TodayFragment;
import com.example.provajava.gui.fragment.TransactionListFragment;

import java.time.LocalDate;

public class TodayActivityPage extends AppCompatActivity implements iActivityManaged{

    private TransactionListFragment listFragment;
    private TodayFragment todayFragment;
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
    }

    public TodayFragment getTodayFragment(){
        return todayFragment;
    }

    private void manageButtons(){

        ImageButton stat = findViewById(R.id.todStatBtn);
        ImageButton pref = findViewById(R.id.statisticsPrefBtn);

        stat.setOnClickListener(v -> {
            Intent intent = new Intent(TodayActivityPage.this, StatisticsActivityPage.class);
            startActivity(intent);
        });

        pref.setOnClickListener(v -> {
            Intent intent = new Intent(TodayActivityPage.this, ToolActivityPage.class);
            startActivity(intent);
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onChangeMonth(TMonth m) {
        month = m;
        if(listFragment!=null){
            listFragment.updateMonth(month);
            listFragment.createOrUpdateView();
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
