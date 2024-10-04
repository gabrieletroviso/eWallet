package com.example.provajava.gui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.provajava.DatabaseHelper;
import com.example.provajava.R;

public class MainActivity extends AppCompatActivity{

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.main);
        manageButtons();

    }

    private void init(){
        DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
        dbh.dailyBackUp();
    }

    private void manageButtons(){

         ImageButton today = findViewById(R.id.todStatBtn);
         ImageButton stats = findViewById(R.id.trdStatBtn);
         ImageButton prefs = findViewById(R.id.prefBtn);

        today.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TodayActivityPage.class);
            startActivity(intent);
        });

        stats.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivityPage.class);
            startActivity(intent);
        });

        prefs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ToolActivityPage.class);
            startActivity(intent);
        });

    }

}
