package com.example.provajava.gui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.provajava.DatabaseHelper;
import com.example.provajava.R;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.gui.activity.MainActivity;
import com.example.provajava.gui.activity.ToolActivityPage;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ToolFragment extends Fragment implements iFragmentManaged {

    private static final String TAG = "ToolFragment";

    private ToolActivityPage listner;
    private View view;
    private DatabaseHelper dbahelper;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    // System file picker (Storage Access Framework): the user picks Google Drive as destination/source
    private final ActivityResultLauncher<String> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"),
            this::onExportDestinationChosen);

    private final ActivityResultLauncher<String[]> importLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::onImportSourceChosen);

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.toolfrag, container, false);
        dbahelper = new DatabaseHelper(requireContext().getApplicationContext());

        manageButton();
        populateView();

        return view;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }

    public void setListner(ToolActivityPage listner){
        this.listner = listner;
    }

    @Override
    public void onFragmentDismissed() {

    }

    @Override
    public void onFragmentClick(long id) {

    }

    @Override
    public void onFragmentNewTransaction(TTransaction tr) {

    }

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    private void manageButton(){
        Button clear = view.findViewById(R.id.clearBtn);
        Button save = view.findViewById(R.id.saveDbBtn);
        Button restore = view.findViewById(R.id.restDbBtn);

        clear.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle("Cancellare tutti i dati?")
                .setMessage("Tutti i movimenti salvati sul telefono verranno eliminati. "
                        + "L'operazione non si può annullare: se non l'hai già fatto, salva prima un backup su Drive.")
                .setPositiveButton("Cancella", (dialog, which) -> {
                    clearDB();
                    Toast.makeText(getContext(), "Dati cancellati", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annulla", null)
                .show());
        save.setOnClickListener(v -> createDBBackup());
        restore.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setMessage("Il database attuale verrà sostituito con il backup scelto e l'app verrà riavviata. Continuare?")
                .setPositiveButton("Sì", (dialog, which) -> importLauncher.launch(new String[]{"*/*"}))
                .setNegativeButton("No", null)
                .show());
    }

    private void populateView(){
        TextView data = view.findViewById(R.id.restData);
        LocalDate lastExport = dbahelper.getLastExportDate();
        if (lastExport == null) {
            data.setText("Nessun backup salvato da questo telefono");
        } else {
            data.setText("Ultimo backup: " + lastExport.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        ((TextView) view.findViewById(R.id.versionText)).setText("eWallet · " + MainActivity.VER);
    }

    private void createDBBackup(){
        String name = "eWallet_backup_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".db";
        exportLauncher.launch(name);
    }

    private void clearDB(){
        listner.dba.clearAllDb();
    }

    private void onExportDestinationChosen(@Nullable Uri uri) {
        if (uri == null) {
            return; // picker cancelled
        }
        Context app = requireContext().getApplicationContext();
        ioExecutor.execute(() -> {
            try {
                dbahelper.exportDatabase(uri);
                runOnUi(() -> {
                    Toast.makeText(app, "Backup salvato", Toast.LENGTH_SHORT).show();
                    populateView();
                });
            } catch (Exception e) {
                Log.e(TAG, "Export failed", e);
                runOnUi(() -> Toast.makeText(app, "Backup non riuscito: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void onImportSourceChosen(@Nullable Uri uri) {
        if (uri == null) {
            return; // picker cancelled
        }
        Context app = requireContext().getApplicationContext();
        ioExecutor.execute(() -> {
            try {
                dbahelper.importDatabase(uri);
                runOnUi(() -> {
                    Toast.makeText(app, "Backup ripristinato, riavvio...", Toast.LENGTH_SHORT).show();
                    restartApp(app);
                });
            } catch (Exception e) {
                Log.e(TAG, "Import failed", e);
                runOnUi(() -> Toast.makeText(app, "Ripristino non riuscito: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void runOnUi(Runnable r) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) r.run();
            });
        }
    }

    // Restart the whole process, so every component reopens the restored db from scratch
    private static void restartApp(Context app) {
        Intent launch = app.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        if (launch != null && launch.getComponent() != null) {
            app.startActivity(Intent.makeRestartActivityTask(launch.getComponent()));
        }
        Runtime.getRuntime().exit(0);
    }

}
