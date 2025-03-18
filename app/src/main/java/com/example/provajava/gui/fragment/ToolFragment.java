package com.example.provajava.gui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.time.LocalDate;

public class ToolFragment extends Fragment implements iFragmentManaged {

    private ToolActivityPage listner;
    private View view;
    private DatabaseHelper dbahelper;
    private boolean isThereBkUp;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.toolfrag, container, false);
        dbahelper = new DatabaseHelper(listner.getApplicationContext());

        manageButton();
        populateView();

        return view;

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

        clear.setOnClickListener(v -> clearDB());
        save.setOnClickListener(v -> createDBBackup());
        restore.setOnClickListener(v -> {

            new AlertDialog.Builder(getActivity())
                    .setMessage("Necessario riavviare l'app. Continuare?")
                    .setPositiveButton("Sì", (dialog, which) -> {
                        try {
                            restoreDBFromBackup();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void populateView(){

        TextView data = view.findViewById(R.id.restData);
        LocalDate bData = dbahelper.getBackupData();
//        try {
//            isThereBkUp = GoogleDriveHelper.checkFileInAppDataUpper(getContext());
//        }catch (Exception ex){}

    }

    private void createDBBackup(){
        saveDBOnDrive();
    }

    private void clearDB(){
        listner.dba.clearAllDb();
    }

    private void restoreDBFromBackup() throws IOException {
        if(isThereBkUp){
//            dbahelper.restoreDB();
            //GoogleDriveHelper.downloadDBFromDrive(getContext(), true);
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().getParent().finish();
        }else{
            throw new IOException("Nessun backup presente");
        }
    }

    // Save database components on google drive.
    // Saving happen on specific thread
    public void saveDBOnDrive(){
        //GoogleDriveHelper.saveDBOnDrive(getContext(), true);
    }

}
