package com.example.provajava;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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

    // ---------------------------------------------------------------------------------------------
    // Export / import through the Storage Access Framework (e.g. Google Drive picked by the user).
    // Both methods do blocking I/O (Drive may hit the network): call them off the main thread.
    // ---------------------------------------------------------------------------------------------

    private static final String IMPORT_TMP = "eWalletDB_import.tmp";
    private static final String BEFORE_RESTORE = "eWalletDB_before_restore.db";
    private static final String PREFS = "ewallet_prefs";
    private static final String PREF_LAST_EXPORT = "last_drive_export";

    // Write a consistent copy of the db to the given document uri
    public void exportDatabase(Uri destination) throws IOException {

        // Room works in WAL mode: flush the -wal file into the main db file before copying,
        // otherwise the most recent changes could be missing from the backup.
        Cursor c = Database.getInstance(context).getOpenHelper().getWritableDatabase()
                .query("PRAGMA wal_checkpoint(FULL)");
        c.moveToFirst();
        c.close();

        File dbFile = context.getDatabasePath(Tools.DATABASE_NAME);
        if (!dbFile.exists()) {
            throw new IOException("Database non trovato");
        }

        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = context.getContentResolver().openOutputStream(destination, "w")) {
            if (out == null) {
                throw new IOException("Impossibile scrivere sul file scelto");
            }
            copy(in, out);
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong(PREF_LAST_EXPORT, System.currentTimeMillis()).apply();
    }

    // Replace the current db with the one read from the given document uri.
    // The app must be restarted afterwards.
    public void importDatabase(Uri source) throws IOException {

        File dbFile = context.getDatabasePath(Tools.DATABASE_NAME);
        File dbDir = dbFile.getParentFile();
        File tmp = new File(dbDir, IMPORT_TMP);

        // 1. Download into a temp file, so a failed/partial read never touches the real db
        try (InputStream in = context.getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(tmp)) {
            if (in == null) {
                throw new IOException("Impossibile leggere il file scelto");
            }
            copy(in, out);
        } catch (IOException e) {
            tmp.delete();
            throw e;
        }

        // 2. Check that it really is an eWallet db with a supported schema version
        try {
            validateBackup(tmp);
        } catch (IOException e) {
            tmp.delete();
            throw e;
        }

        // 3. Close Room, keep a copy of the current db, then swap the files
        Database.closeInstance();

        if (dbFile.exists()) {
            File before = new File(dbDir, BEFORE_RESTORE);
            before.delete();
            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = new FileOutputStream(before)) {
                copy(in, out);
            }
        }

        new File(dbDir, Tools.DATABASE_NAME + "-wal").delete();
        new File(dbDir, Tools.DATABASE_NAME + "-shm").delete();
        new File(dbDir, Tools.DATABASE_NAME + "-journal").delete();
        dbFile.delete();

        if (!tmp.renameTo(dbFile)) {
            tmp.delete();
            throw new IOException("Impossibile sostituire il database");
        }
    }

    // Date of the last export done from this device, null if none
    public LocalDate getLastExportDate() {
        long ts = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(PREF_LAST_EXPORT, 0);
        if (ts == 0) {
            return null;
        }
        return Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void validateBackup(File file) throws IOException {
        SQLiteDatabase db;
        try {
            db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            throw new IOException("Il file scelto non è un database valido");
        }
        try {
            if (DatabaseUtils.queryNumEntries(db, "sqlite_master",
                    "type = 'table' AND name = 'T_Transaction'") == 0) {
                throw new IOException("Il file scelto non è un backup di eWallet");
            }
            if (db.getVersion() > Database.DB_VERSION) {
                throw new IOException("Backup creato con una versione più recente dell'app");
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Il file scelto non è un database valido");
        } finally {
            db.close();
            // Opening a WAL-mode file leaves -wal/-shm companions next to it: remove them
            new File(file.getPath() + "-wal").delete();
            new File(file.getPath() + "-shm").delete();
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}