package com.example.provajava.gui.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

// Abstract class which manage dismissing fragment
// using fragment managed interface for callback.
// All fragments need to extends this abstract class
public abstract class ManagedDialog extends DialogFragment {

    protected iFragmentManaged listner;

    protected ManagedDialog(iFragmentManaged parent){
        this.listner = parent;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    // Callback when dismiss fragment
    protected void dismissFragment() {
        if (listner != null) {
            listner.onFragmentDismissed();
        }
        dismiss();
    }

    // Callback when new transaction
    protected void onNewTransaction(TTransaction t){
        if(listner!=null){
            listner.onFragmentNewTransaction(t);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setView(onCreateView(getLayoutInflater(), null, savedInstanceState))
                    .create();
            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            return alertDialog;
        }
        return null;
    }

}
