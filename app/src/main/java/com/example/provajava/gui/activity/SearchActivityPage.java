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
import com.example.provajava.gui.fragment.TransactionFilterFragment;
import com.example.provajava.gui.fragment.TransactionSearchFragment;

import java.time.LocalDate;
import java.util.List;

public class SearchActivityPage extends AppCompatActivity implements iActivityManaged{

    private TransactionSearchFragment searchFragment;
    private TransactionFilterFragment filterFragment;
    public DatabaseAccess dba;
    public TYear year;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchpage);

        ViewPager2 viewPager = findViewById(R.id.searchViewPage);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        dba = new DatabaseAccess(getApplicationContext());
        year = dba.getYear(LocalDate.now().getYear());

        manageButtons();
    }

    private void manageButtons(){

        ImageButton today = findViewById(R.id.todayStatBtn);
        ImageButton stats = findViewById(R.id.todStatBtn);
        ImageButton pref = findViewById(R.id.statisticsPrefBtn);

        today.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivityPage.this, TodayActivityPage.class);
            startActivity(intent);
        });

        stats.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivityPage.this, StatisticsActivityPage.class);
            startActivity(intent);
        });
        pref.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivityPage.this, ToolActivityPage.class);
            startActivity(intent);
        });

    }

    @Override
    public void onChangeMonth(TMonth m) {
    }

    @Override
    public void onNewTransaction(TTransaction tr) {
    }

    public DatabaseAccess getDba(){
        return dba;
    }

    public void populateTransaction(List<TTransaction> trss){
        searchFragment.populateTransaction(trss);
        switchToListTransaction();
    }

    private void switchToListTransaction() {
        ViewPager2 viewPager = findViewById(R.id.searchViewPage);
        viewPager.setCurrentItem(0, true);
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {

            if (position == 1) {
                filterFragment = new TransactionFilterFragment();
                filterFragment.setListner(SearchActivityPage.this);
                return filterFragment;
            } else {
                searchFragment = new TransactionSearchFragment();
                searchFragment.setListner(SearchActivityPage.this);
                return searchFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

    }
}
