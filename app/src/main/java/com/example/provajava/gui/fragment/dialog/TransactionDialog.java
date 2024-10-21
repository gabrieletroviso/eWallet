package com.example.provajava.gui.fragment.dialog;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.provajava.R;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.enumerator.eTranMainType;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.gui.activity.TodayActivityPage;
import com.example.provajava.gui.fragment.TodayFragment;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class TransactionDialog extends ManagedDialog {

    boolean isIncome;
    DatabaseAccess dba;
    ArrayList<Integer> switches;
    eTranSubType type = eTranSubType.NONE;
    LocalDate chosenDate = null;
    TTransaction transaction = null;

    public TransactionDialog(DatabaseAccess dba, boolean isInc, TTransaction trans, iFragmentManaged parent){
        super(parent);
        this.dba = dba;
        this.isIncome = isInc;
        this.switches = new ArrayList<>();
        this.transaction = trans;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;
        switches.clear();
        if(transaction!=null){
            chosenDate = Instant.ofEpochMilli(transaction.getDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        else{
            chosenDate = ((TodayFragment)listner).day;
        }

        if(isIncome){
            view = inflater.inflate(R.layout.incomefrag, container, false);
            switches.add(R.id.salSw);
            switches.add(R.id.othSw);

            Switch salSw = view.findViewById(R.id.salSw);
            Switch othSw = view.findViewById(R.id.othSw);
            salSw.setOnClickListener(v -> handleSwitch(view, R.id.salSw, eTranSubType.SALARY, true));
            othSw.setOnClickListener(v -> handleSwitch(view, R.id.othSw, eTranSubType.OTHER_INCOME, true));

            FloatingActionButton ok = view.findViewById(R.id.incBtn);
            ok.setOnClickListener(v -> handleIncomeBtn(view));

            if(transaction!=null){

                EditText val = view.findViewById(R.id.incValue);
                EditText desc = view.findViewById(R.id.descIncValue);

                val.setText(String.valueOf(transaction.getAmount()));
                desc.setText(transaction.getDescription());

                switch (transaction.getTranSubType()){
                    case OTHER_INCOME:
                        othSw.setChecked(true);
                        othSw.callOnClick();
                        break;
                    default:
                        salSw.setChecked(true);
                        salSw.callOnClick();
                        break;
                }
            }

        }else{
            view = inflater.inflate(R.layout.expensefrag, container, false);
            switches.add(R.id.unnSw);
            switches.add(R.id.necSw);
            switches.add(R.id.extSw);

            Switch unnSw = view.findViewById(R.id.unnSw);
            Switch necSw = view.findViewById(R.id.necSw);
            Switch extSw = view.findViewById(R.id.extSw);

            unnSw.setOnClickListener(v -> handleSwitch(view, R.id.unnSw, eTranSubType.UNNECESSARY, false));
            necSw.setOnClickListener(v -> handleSwitch(view, R.id.necSw, eTranSubType.NECESSARY, false));
            extSw.setOnClickListener(v -> handleSwitch(view, R.id.extSw, eTranSubType.EXTRA, false));

            FloatingActionButton ok = view.findViewById(R.id.expBtn);
            ok.setOnClickListener(v -> handleExpBtn(view));

            if(transaction!=null) {

                EditText val = view.findViewById(R.id.expValue);
                EditText desc = view.findViewById(R.id.descValue);

                val.setText(String.valueOf(transaction.getAmount()));
                desc.setText(transaction.getDescription());

                switch (transaction.getTranSubType()) {
                    case UNNECESSARY:
                        unnSw.setChecked(true);
                        unnSw.callOnClick();
                        break;
                    case EXTRA:
                        extSw.setChecked(true);
                        extSw.callOnClick();
                        break;
                    default:
                        necSw.setChecked(true);
                        necSw.callOnClick();
                        break;
                }
            }
        }

        manageDateButton(view);

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleIncomeBtn(View v){

        String income = ((EditText)v.findViewById(R.id.incValue)).getText().toString();
        String desc = ((EditText)v.findViewById(R.id.descIncValue)).getText().toString();

        if (TextUtils.isEmpty(income) || eTranSubType.NONE.equals(type)) {
            Toast.makeText(getActivity(), "Per favore, inserire valore e tipo per la transazione", Toast.LENGTH_SHORT).show();
        }else{

            TTransaction t;

            if(transaction!=null){
                transaction.setAmount(Double.valueOf(income));
                transaction.setDescription(desc);
                transaction.setTranSubType(type);
                transaction.setDate(chosenDate.atStartOfDay().
                        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                dba.updateTransaction(transaction);
                t = transaction;
            }else{
                t = dba.addTransaction(chosenDate, Double.valueOf(income), type, desc);
            }

            onNewTransaction(t);
            dismissFragment();

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleExpBtn(View v){

        String expense = ((EditText)v.findViewById(R.id.expValue)).getText().toString();
        String desc = ((EditText)v.findViewById(R.id.descValue)).getText().toString();

        if (TextUtils.isEmpty(expense) || eTranSubType.NONE.equals(type)) {
            Toast.makeText(getActivity(), "Per favore, inserire valore e tipo per la transazione", Toast.LENGTH_SHORT).show();
        }else{

            TTransaction t;

            if(transaction!=null){
                transaction.setAmount(Double.valueOf(expense));
                transaction.setDescription(desc);
                transaction.setTranSubType(type);
                transaction.setDate(chosenDate.atStartOfDay().
                        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                dba.updateTransaction(transaction);
                t = transaction;
            }else{
                t = dba.addTransaction(chosenDate, Double.valueOf(expense), type, desc);
            }

            onNewTransaction(t);
            dismissFragment();
        }
    }

    // Manage switch after selection
    private void handleSwitch(View v, Integer key, eTranSubType selected, boolean inc){

        ColorStateList c = ColorStateList.valueOf(getResources().getColor(R.color.buttondefault, null));
        ColorStateList ctr = ColorStateList.valueOf(getResources().getColor(R.color.trackdefault, null));

        if(((Switch)v.findViewById(key)).isChecked()) {
            type = selected;
            if(inc){
                c = ColorStateList.valueOf(getResources().getColor(R.color.dark_green, null));
            }else{
                c = ColorStateList.valueOf(getResources().getColor(R.color.dark_red, null));
            }
            ((Switch)v.findViewById(key)).setThumbTintList(c);
            ((Switch)v.findViewById(key)).setTrackTintList(c);

        }else {
            type = eTranSubType.NONE;
            ((Switch)v.findViewById(key)).setThumbTintList(c);
            ((Switch)v.findViewById(key)).setTrackTintList(ctr);
        }

        cleanSwitches(v,key);
    }

    // Clean all switches
    private void cleanSwitches(View v, Integer key){

        ColorStateList c = ColorStateList.valueOf(getResources().getColor(R.color.buttondefault, null));
        ColorStateList ctr = ColorStateList.valueOf(getResources().getColor(R.color.trackdefault, null));
        if(!type.equals(eTranSubType.NONE)){
            for (Integer s : switches) {
                if(!s.equals(key)){
                    ((Switch)v.findViewById(s)).setChecked(false);
                    ((Switch)v.findViewById(s)).setThumbTintList(c);
                    ((Switch)v.findViewById(s)).setTrackTintList(ctr);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void manageDateButton(View view){

        Button date = view.findViewById(isIncome ? R.id.dateBtn2 : R.id.dateBtn);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        date.setText(chosenDate.format(formatter));
        date.setOnClickListener(v -> {

            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setCalendarConstraints(setCalendarConstraint().build())
                    .setTitleText("Seleziona una data")
                    .setTheme(R.style.MaterialCalendarTheme);

            MaterialDatePicker<Long> datePicker = builder.build();
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    chosenDate = Instant.ofEpochMilli(datePicker.getSelection()).atZone(ZoneId.systemDefault()).toLocalDate();;
                    date.setText(chosenDate.format(formatter));
                }
            });
        });
    }

    private CalendarConstraints.Builder setCalendarConstraint(){

        TMonth m = getMonth();
        TYear y = dba.getYearById(m.getYearId());

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startCalendar.set(y.getYear(), m.getMonth() - 1, 1, 0, 0, 0);
        long startDate = startCalendar.getTimeInMillis();

        Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endCalendar.set(y.getYear(), m.getMonth() - 1, 1);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        long endDate = endCalendar.getTimeInMillis();

        constraintsBuilder.setStart(startDate);
        constraintsBuilder.setEnd(endDate);

        return constraintsBuilder;
    }

    private TMonth getMonth(){
        TodayActivityPage todPage = (TodayActivityPage) listner.getParentPage();
        return todPage.getMonth();
    }
}