package com.example.provajava.gui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.provajava.DatabaseHelper;
import com.example.provajava.R;
import com.example.provajava.capture.PaymentNotificationListener;
import com.example.provajava.gui.activity.CapturedNotificationsActivity;
import com.example.provajava.gui.ConfirmDialog;
import com.example.provajava.gui.Notice;
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

        clear.setOnClickListener(v -> ConfirmDialog.destructive(requireContext(), R.drawable.ic_trash,
                "Cancellare tutti i dati?",
                "Tutti i movimenti salvati sul telefono verranno eliminati. "
                        + "L'operazione non si può annullare: se non l'hai già fatto, salva prima un backup su Drive.",
                "Cancella",
                () -> {
                    clearDB();
                    Notice.success(getActivity(), "Dati cancellati");
                }));
        save.setOnClickListener(v -> createDBBackup());

        view.findViewById(R.id.notifAccessBtn).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        view.findViewById(R.id.notifListBtn).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CapturedNotificationsActivity.class)));
        restore.setOnClickListener(v -> ConfirmDialog.normal(requireContext(), R.drawable.ic_restore,
                "Ripristinare un backup?",
                "Il database attuale verrà sostituito con il backup che scegli e l'app verrà riavviata.",
                "Scegli backup",
                () -> importLauncher.launch(new String[]{"*/*"})));
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
        populateNotificationStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        // The user may come back from the system "Accesso alle notifiche" screen
        if (view != null) populateNotificationStatus();
    }

    private void populateNotificationStatus() {
        boolean granted = PaymentNotificationListener.isAccessGranted(requireContext());
        TextView status = view.findViewById(R.id.notifStatus);
        status.setText(granted ? "Attiva" : "Non attiva: tocca Accesso e abilita eWallet");
        status.setTextColor(ContextCompat.getColor(requireContext(),
                granted ? R.color.ew_income : R.color.ew_text_secondary));
        ((Button) view.findViewById(R.id.notifAccessBtn)).setText(granted ? "Gestisci" : "Attiva");
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
                    Notice.success(getActivity(), "Backup salvato");
                    populateView();
                });
            } catch (Exception e) {
                Log.e(TAG, "Export failed", e);
                runOnUi(() -> Notice.error(getActivity(), "Backup non riuscito: " + e.getMessage()));
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
                    // Leave the message on screen for a moment before restarting
                    Notice.success(getActivity(), "Backup ripristinato, riavvio...");
                    view.postDelayed(() -> restartApp(app), 1200);
                });
            } catch (Exception e) {
                Log.e(TAG, "Import failed", e);
                runOnUi(() -> Notice.error(getActivity(), "Ripristino non riuscito: " + e.getMessage()));
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
