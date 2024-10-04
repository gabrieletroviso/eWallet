package com.example.provajava.dbmanager;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.provajava.Tools;
import com.example.provajava.datamodel.TDay;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.querydao.*;

@androidx.room.Database(entities = {TTransaction.class, TDay.class, TMonth.class, TYear.class}, version = 1)
public abstract class Database extends RoomDatabase {
    
    private static volatile Database INSTANCE;
    
    public abstract TTransactionDAO getTransactionDAO();
    public abstract TDayDAO getDayDAO();
    public abstract TMonthDAO getMonthDAO();
    public abstract TYearDAO getYearDAO();
    
    public static Database getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            Database.class, Tools.DATABASE_NAME)
                            .build();
        }
        return INSTANCE;
    }
}