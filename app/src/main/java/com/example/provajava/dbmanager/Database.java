package com.example.provajava.dbmanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.provajava.Tools;
import com.example.provajava.datamodel.TDay;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.querydao.*;

import java.util.List;

@androidx.room.Database(entities = {TTransaction.class, TDay.class, TMonth.class, TYear.class}, version = 2)
public abstract class Database extends RoomDatabase {
    
    private static volatile Database INSTANCE;
    
    public abstract TTransactionDAO getTransactionDAO();
    public abstract TDayDAO getDayDAO();
    public abstract TMonthDAO getMonthDAO();
    public abstract TYearDAO getYearDAO();
    
    public static Database getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            Database.class, Tools.DATABASE_NAME).addMigrations(MIGRATION_1_2)
                            .build();
        }
        return INSTANCE;

//        if (INSTANCE == null) {
//            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
//                            Database.class, Tools.DATABASE_NAME)
//                    .addMigrations(MIGRATION_1_2)
//                    .setQueryCallback(new RoomDatabase.QueryCallback() {
//                        @Override
//                        public void onQuery(@NonNull String sqlQuery, @NonNull List<? extends Object> bindArgs) {
//                            // Log the SQL query and bindings
//                            android.util.Log.d("SQL_LOG", "Query: " + sqlQuery);
//                            android.util.Log.d("SQL_LOG", "Bindings: " + bindArgs.toString());
//                        }
//                    }, ContextCompat.getMainExecutor(context))
//                    .build();
//        }
//        return INSTANCE;

    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE T_Month ADD COLUMN TotSalIncome REAL NOT NULL DEFAULT 0");
        }
    };
}