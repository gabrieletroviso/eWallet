package com.example.provajava.gui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.gui.activity.TodayActivityPage;
import com.example.provajava.gui.bottomsheet.BottomSheetMonth;
import com.example.provajava.gui.fragment.dialog.TransactionDialog;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class TodayFragment extends Fragment implements iFragmentManaged {

    private TodayActivityPage listner;
    private View view;
    private DatabaseAccess dba;
    private TMonth month;
    public LocalDate day;

    @Override
    public void onFragmentNewTransaction(TTransaction tr) {
        listner.onNewTransaction(tr);
    }

    @Override
    public void onFragmentDismissed(){
        populateValues();
    }

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onFragmentClick(long id) {

        month = dba.getMonthById(id);
        listner.onChangeMonth(month);

        if(month.getMonth()==LocalDate.now().getMonthValue()){
            day = LocalDate.now();
        }else{
            day = LocalDate.of(dba.getYearById(month.getYearId()).getYear(), month.getMonth(), 1);
        }

        populateFixField();
        populateValues();
        manageButtons();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            month = getArguments().getParcelable("month");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.today, container, false);
        day = LocalDate.now();
        populateView(view);
        return view;

    }

    public View updateView(){

        View view = getView();
        populateView(view);
        return view;
    }

    public void setListner(TodayActivityPage listner){
        this.listner = listner;
        this.month = listner.getMonth();
        this.dba = listner.getDba();
    }

    @SuppressLint("NewApi")
    private void populateFixField(){

        Button monthBtn = view.findViewById(R.id.monthBtn);
        TextView yearLbl = view.findViewById(R.id.yearTodLabel);
        monthBtn.setText(Month.of(month.getMonth()).getDisplayName(TextStyle.FULL, Locale.ITALIAN));
        yearLbl.setText(String.valueOf(dba.getYearById(month.getYearId()).getYear()));

        monthBtn.setOnClickListener(v -> {
            BottomSheetMonth bottomSheet = new BottomSheetMonth(this, dba);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
    }

    private void populateValues(){

        TextView inc = view.findViewById(R.id.incValue);
        TextView exp = view.findViewById(R.id.expValue);
        TextView bal = view.findViewById(R.id.balValue);
        ImageView img = view.findViewById(R.id.balImg);

        if(month == null){
            inc.setText("0");
            exp.setText("0");
            bal.setText("0");

        }else{

            double e = dba.getMonthlyTotExpenses(month.getId());
            double i = dba.getMonthlyTotIncomes(month.getId());
            double b = i-e;

            exp.setText(Tools.roundToPrint(e));
            inc.setText(Tools.roundToPrint(i));
            bal.setText(Tools.roundToPrint(b));
            if(b==0){
                bal.setTextColor(Color.GRAY);
                img.setImageDrawable(getResources().getDrawable(R.drawable.draw, null));
            }else if(b>0){
                bal.setTextColor(getResources().getColor(R.color.dark_green, null));
                img.setImageDrawable(getResources().getDrawable(R.drawable.up, null));
            }else{
                bal.setTextColor(getResources().getColor(R.color.dark_red, null));
                img.setImageDrawable(getResources().getDrawable(R.drawable.down, null));
            }

        }
    }

    private void manageButtons(){

        FloatingActionButton addButton = view.findViewById(R.id.addBtn);
        FloatingActionButton expButton = view.findViewById(R.id.expBtn);

        addButton.setOnClickListener(v -> {
            DialogFragment dialog = new TransactionDialog(dba, true, null, this);
            dialog.show(getParentFragmentManager(), "Income");
        });

        expButton.setOnClickListener(v -> {
            DialogFragment dialog = new TransactionDialog(dba, false, null, this);
            dialog.show(getParentFragmentManager(), "Expense");
        });
    }

    // If is new month, it will create dummy transaction in order
    // to add month record on db. Next, this transaction is deleted
    private void checkMonth() {
        if (month == null) {
            TTransaction none = dba.addTransaction(day, -1, eTranSubType.NONE);
            month = dba.getMonthById(none.getMonthId());
            dba.deleteTransaction(none);
        }
    }

    private View populateView(View view){

        checkMonth();
        populateFixField();
        populateValues();
        manageButtons();

        return view;
    }
}
