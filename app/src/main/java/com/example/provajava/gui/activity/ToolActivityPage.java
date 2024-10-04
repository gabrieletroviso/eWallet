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
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.fragment.ToolFragment;

public class ToolActivityPage extends AppCompatActivity {

    private ToolFragment toolFragment;
    public DatabaseAccess dba;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolpage);

        ViewPager2 viewPager = findViewById(R.id.toolViewPage);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        dba = new DatabaseAccess(getApplicationContext());

        manageToolbarButtons();
    }

    private void manageToolbarButtons(){

        ImageButton today = findViewById(R.id.todToolBtn);
        ImageButton stats = findViewById(R.id.statisticsToolBtn);

        today.setOnClickListener(v -> {
            Intent intent = new Intent(ToolActivityPage.this, TodayActivityPage.class);
            startActivity(intent);
        });

        stats.setOnClickListener(v -> {
            Intent intent = new Intent(ToolActivityPage.this, StatisticsActivityPage.class);
            startActivity(intent);
        });

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {

            toolFragment = new ToolFragment();
            toolFragment.setListner(ToolActivityPage.this);
            return toolFragment;

        }

        @Override
        public int getItemCount() {
            return 1;
        }

    }

}
