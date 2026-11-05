package com.example.provajava.gui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.provajava.DatabaseHelper;

// Launcher: prepares the app and opens Home (Mese) right away.
// The old menu screen (layout/main.xml) has been replaced by the bottom navigation bar.
public class MainActivity extends AppCompatActivity{

    public static final String VER = "Versione 1.1 Beta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        init();

        startActivity(new Intent(this, TodayActivityPage.class));
        finish();
        overridePendingTransition(0, 0);
    }

    private void init(){
        DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
        dbh.databaseBackUp();
    }
}
