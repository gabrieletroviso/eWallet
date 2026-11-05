package com.example.provajava.gui.activity;

import android.os.Build;
import android.os.Bundle;

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
import com.example.provajava.gui.BottomNav;
import com.example.provajava.gui.PillTabs;
import com.example.provajava.gui.fragment.TransactionFilterFragment;
import com.example.provajava.gui.fragment.TransactionSearchFragment;

import java.time.LocalDate;
import java.util.List;

public class SearchActivityPage extends AppCompatActivity implements iActivityManaged{

    private static final int PAGE_FILTERS = 0;
    private static final int PAGE_RESULTS = 1;

    private TransactionSearchFragment searchFragment;
    private List<TTransaction> lastResults;
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
        viewPager.setOffscreenPageLimit(1);
        PillTabs.bind(viewPager, findViewById(R.id.tabFilters), findViewById(R.id.tabResults));

        dba = new DatabaseAccess(getApplicationContext());
        year = dba.getYear(LocalDate.now().getYear());

        manageButtons();
    }

    private void manageButtons(){
        BottomNav.setup(this, R.id.nav_search);
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

    // Results of the last search (null = no search done yet), kept here so the Risultati page
    // can show them even if it is created after the search
    public List<TTransaction> getLastResults(){
        return lastResults;
    }

    public void populateTransaction(List<TTransaction> trss){
        lastResults = trss;
        if(searchFragment != null && searchFragment.getView() != null){
            searchFragment.populateTransaction(trss);
        }
        switchToListTransaction();
    }

    private void switchToListTransaction() {
        ViewPager2 viewPager = findViewById(R.id.searchViewPage);
        viewPager.setCurrentItem(PAGE_RESULTS, true);
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {

            if (position == PAGE_FILTERS) {
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
