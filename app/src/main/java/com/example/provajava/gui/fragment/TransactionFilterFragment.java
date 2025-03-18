package com.example.provajava.gui.fragment;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.gui.activity.SearchActivityPage;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionFilterFragment extends Fragment implements iFragmentManaged {

    private SearchActivityPage listner;
    DatabaseAccess dba;
    private List<eTranSubType> types;
    private List<TTransaction> trss;
    private LocalDate from;
    private LocalDate to;

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

    public void setListner(SearchActivityPage listner){
        this.listner = listner;
        this.dba = listner.dba;
    }

    public TransactionFilterFragment(){
        this.trss = new ArrayList<>();
        this.types = new ArrayList<>();
        this.from = LocalDate.now().atTime(0,0,0,0)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        this.to = LocalDate.now().atTime(23, 59, 59, 999999999)
                .atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.searchfilter, null);
        types.clear();

        setDateBtn(view, R.id.fromBtn);
        setDateBtn(view, R.id.toBtn);
        EditText moreThen = view.findViewById(R.id.moreVal);
        EditText lessThen = view.findViewById(R.id.lessVal);
        EditText desc = view.findViewById(R.id.descVal);
        ImageButton run = view.findViewById(R.id.searchBtn);

        Switch salSw = view.findViewById(R.id.salChip4);
        Switch othSw = view.findViewById(R.id.othChip);
        Switch necSw = view.findViewById(R.id.necChip);
        Switch unnSw = view.findViewById(R.id.unnChip);
        Switch extSw = view.findViewById(R.id.extChip);
        salSw.setOnClickListener(v -> handleSwitch(view, R.id.salChip4, eTranSubType.SALARY, true));
        othSw.setOnClickListener(v -> handleSwitch(view, R.id.othChip, eTranSubType.OTHER_INCOME, true));
        unnSw.setOnClickListener(v -> handleSwitch(view, R.id.unnChip, eTranSubType.UNNECESSARY, false));
        necSw.setOnClickListener(v -> handleSwitch(view, R.id.necChip, eTranSubType.NECESSARY, false));
        extSw.setOnClickListener(v -> handleSwitch(view, R.id.extChip, eTranSubType.EXTRA, false));

        run.setOnClickListener(v -> searchAndPopulate(types, moreThen, lessThen, desc.getText().toString()));

        return view;
    }

    private void setDateBtn(View popupView, int id) {

        Button btn = popupView.findViewById(id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        final LocalDate[] chosenDate = {LocalDate.now()};

        btn.setText(chosenDate[0].format(formatter));
        btn.setOnClickListener(v -> {

            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText("Seleziona una data")
                    .setTheme(R.style.MaterialCalendarTheme);

            MaterialDatePicker<Long> datePicker = builder.build();
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    chosenDate[0] = Instant.ofEpochMilli(datePicker.getSelection()).atZone(ZoneId.systemDefault()).toLocalDate();
                    btn.setText(chosenDate[0].format(formatter));
                    if(id==R.id.fromBtn) {
                        from = chosenDate[0];
                    } else{
                        to = chosenDate[0].atTime(23, 59, 59, 999999999)
                                .atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                }
            });
        });

    }

    private void handleSwitch(View v, Integer key, eTranSubType selected, boolean inc) {

        ColorStateList c = ColorStateList.valueOf(getResources().getColor(R.color.buttondefault, null));
        ColorStateList ctr = ColorStateList.valueOf(getResources().getColor(R.color.trackdefault, null));

        if (((Switch) v.findViewById(key)).isChecked()) {

            types.add(selected);
            if (inc) {
                c = ColorStateList.valueOf(getResources().getColor(R.color.dark_green, null));
            } else {
                c = ColorStateList.valueOf(getResources().getColor(R.color.dark_red, null));
            }

            ((Switch) v.findViewById(key)).setThumbTintList(c);
            ((Switch) v.findViewById(key)).setTrackTintList(c);

        } else {
            types.remove(selected);
            ((Switch) v.findViewById(key)).setThumbTintList(c);
            ((Switch) v.findViewById(key)).setTrackTintList(ctr);
        }

    }

    private void searchAndPopulate(List<eTranSubType> types, EditText moreThen, EditText lessThen, String desc){

        String moreStr = moreThen.getText().toString();
        String lessStr = lessThen.getText().toString();

        Double more = moreStr.isEmpty() ? null : Double.valueOf(moreStr);
        Double less = lessStr.isEmpty() ? null : Double.valueOf(lessStr);

        if(valuesCheck(more, less)){
            trss = dba.getFilteredTransactionsV2(types.isEmpty() ? null : types,
                    from.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    to.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    more, less, desc.isEmpty() ? null : desc);

            if(trss!=null){
                listner.populateTransaction(trss);
            }

        }

        // Passa alla lista
    }
    private boolean valuesCheck(Double moreThen, Double lessThen){

        boolean ret = true;

        if(moreThen!=null && lessThen !=null){
            if(moreThen!=null & lessThen!=null){
                if(moreThen>lessThen){
                    Toast.makeText(getActivity(),
                            "Il limite superiore deve essere maggiore o uguale a quello inferiore",
                            Toast.LENGTH_SHORT).show();
                    ret = false;
                }else if(lessThen<moreThen){
                    Toast.makeText(getActivity(),
                            "Il limite inferiore deve essere minore o uguale a quello superiore",
                            Toast.LENGTH_SHORT).show();
                    ret = false;
                }else if(from.isAfter(to)){
                    Toast.makeText(getActivity(),
                            "La data di inizio deve essere precedente o uguale a quella di fine",
                            Toast.LENGTH_SHORT).show();
                    ret = false;
                }else if(to.isBefore(from)){
                    Toast.makeText(getActivity(),
                            "La data di fine deve essere successiva o uguale a quella di inizio",
                            Toast.LENGTH_SHORT).show();
                    ret = false;
                }
            }
        }

        return ret;
    }

}
