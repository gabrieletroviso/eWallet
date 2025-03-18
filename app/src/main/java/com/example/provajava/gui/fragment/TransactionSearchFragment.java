package com.example.provajava.gui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.enumerator.eTranMainType;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.gui.activity.SearchActivityPage;
import com.example.provajava.gui.fragment.dialog.TransactionDialog;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionSearchFragment extends Fragment implements iFragmentManaged {

    private SearchActivityPage listner;
    private DatabaseAccess dba;
    private TableLayout tableLayout;
    private ConstraintLayout cstLy;

    @Override
    public void onFragmentDismissed() {

    }

    @Override
    public void onFragmentClick(long id) {

    }

    @Override
    public void onFragmentNewTransaction(TTransaction tr) {
        listner.onNewTransaction(tr);
    }

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.searchlist, container, false);
        view = populateView(view);
        return view;

    }

    public void setListner(SearchActivityPage listner){
        this.listner = listner;
        this.dba = listner.getDba();
    }

    private View populateView(View view){
        cstLy = view.findViewById(R.id.innerConstraintLayout);
        tableLayout = view.findViewById(R.id.tableLayout);
        tableLayout.addView(getDefaultRow("Swipe per filtrare"));
        return view;
    }

    public void populateTransaction(List<TTransaction> trss){

        if (tableLayout.getChildCount() > 0) {
            tableLayout.removeViews(0, tableLayout.getChildCount());
        }

        if(!trss.isEmpty()){
            for (TTransaction t : trss) {
                TableRow tableRow = getTableRow(t);
                tableLayout.addView(tableRow);
            }
        }else{
            tableLayout.addView(getDefaultRow("Nessuna transazione trovata.\n Swipe per nuovi filtri."));
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public TableRow getTableRow(TTransaction tran){

        TableRow tableRow = (TableRow) LayoutInflater.from(getContext())
                .inflate(R.layout.trnsearchitem, tableLayout, false);
        tableRow.setTag(tran);

        TextView day = tableRow.findViewById(R.id.day);
        TextView subtype = tableRow.findViewById(R.id.subtype);
        TextView value = tableRow.findViewById(R.id.value);
        TextView desc = tableRow.findViewById(R.id.desc);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            day.setText(Tools.formattedDate(Instant.ofEpochMilli(tran.getDate())
                    .atZone(ZoneId.systemDefault()).toLocalDate()));

        }
        subtype.setText(tran.getTranSubType().toString());
        value.setText(Tools.roundToPrint(tran.getAmount()));

        if (tran.getTranMainType().equals(eTranMainType.EXPENSE)) {
            value.setText(String.valueOf(tran.getAmount()*-1));
            value.setTextColor(getResources().getColor(R.color.dark_red, null));
        }else{
            value.setTextColor(getResources().getColor(R.color.dark_green, null));
        }

        desc.setText(tran.getDescription());

        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 20);
        tableRow.setLayoutParams(params);

        return tableRow;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public TableRow getDefaultRow(String text){

        TableRow tableRow = (TableRow) LayoutInflater.from(getContext())
                .inflate(R.layout.trnsearchitemnotfound, tableLayout, false);

        TextView txt = tableRow.findViewById(R.id.single_text);
        txt.setText(text);

        return tableRow;
    }


}
