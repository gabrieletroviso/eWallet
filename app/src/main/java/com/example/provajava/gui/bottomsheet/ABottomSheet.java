package com.example.provajava.gui.bottomsheet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.provajava.R;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public abstract class ABottomSheet extends BottomSheetDialogFragment {

    protected iFragmentManaged listner;
    protected List items;
    protected DatabaseAccess dba;

    public ABottomSheet(iFragmentManaged listner, DatabaseAccess dba) {
        this.listner = listner;
        this.dba = dba;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(R.drawable.border);
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        int maxHeight = (int) (300 * getResources().getDisplayMetrics().density); // 400dp massimo
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                maxHeight
        ));

        recyclerView.setAdapter(new BottomSheetElement(getContext(), items, dba,
                position -> {
                    onElementClick(position);
                }));
        return recyclerView;
    }

    protected abstract void onElementClick(int position);

}
