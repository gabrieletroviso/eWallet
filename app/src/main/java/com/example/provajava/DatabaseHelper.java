package com.example.provajava;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.example.provajava.dbmanager.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_BACKUP = "eWalletDB_backup.db";
    private static final String DATABASE_PATH = "/data/data/com.example.provajava/databases/";
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, Tools.DATABASE_NAME, null, 1);
        this.context = context;
    }

    public void dailyBackUp(){
        if(!LocalDate.now().equals(getBackupData())){
            databaseBackUp();
        }
    }

    // Create or override db backup
    public void databaseBackUp(){

        Database.getInstance(context).close();
        String currentDBPath = DATABASE_PATH + Tools.DATABASE_NAME;
        String backupDBPath = DATABASE_PATH + DATABASE_BACKUP;
        File currentDB = new File(currentDBPath);
        File backupDB = new File(backupDBPath);

        if(backupDB.exists()){
            backupDB.delete();
        }

        try {
            if (currentDB.exists()) {
                FileInputStream src = new FileInputStream(currentDB);
                FileOutputStream dst = new FileOutputStream(backupDB);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = src.read(buffer)) > 0) {
                    dst.write(buffer, 0, length);
                }
                dst.flush();
                src.close();
                dst.close();
                getWritableDatabase();
                Database.getInstance(context);
            }
        } catch (IOException e) {
            Log.d("DatabaseExport", e.getLocalizedMessage());
        }
    }

    // Get db backup file data
    public LocalDate getBackupData() {
        File backupFile = new File(DATABASE_PATH + DATABASE_BACKUP);  // "eWalletDB_backup.db"
        if (!backupFile.exists()) {
            return null;
        }

        // Ottieni la data di ultima modifica
        long lastModifiedTimestamp = backupFile.lastModified();
        LocalDate lastModifiedDate = Instant.ofEpochMilli(lastModifiedTimestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        return lastModifiedDate;
    }

    // Restore db from db backup
    public void restoreDB() throws IOException {

        Database.getInstance(context).close();
        File dbFile = new File(DATABASE_PATH + Tools.DATABASE_NAME);  // "eWallet.db"
        File backupFile = new File(DATABASE_PATH + DATABASE_BACKUP);  // "eWalletDB_backup.db"

        if (!backupFile.exists()) {
            throw new IOException("Backup database non trovato " + backupFile.getAbsolutePath());
        }

        if (dbFile.exists()) {
            dbFile.delete();
        }

        InputStream input = new FileInputStream(backupFile);
        OutputStream output = new FileOutputStream(dbFile);

        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e("DatabaseRestore", "Errore durante il ripristino del database", e);
        }



        output.flush();
        output.close();
        input.close();
        getWritableDatabase();
        Database.getInstance(context);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}