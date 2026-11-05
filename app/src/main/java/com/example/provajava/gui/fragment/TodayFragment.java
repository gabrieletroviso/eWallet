package com.example.provajava.gui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.time.LocalDate;

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

    // Month picker (bottom sheet), opened by tapping the month title in the page header
    public void openMonthPicker(){
        BottomSheetMonth bottomSheet = new BottomSheetMonth(this, dba);
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    private void populateValues(){

        TextView inc = view.findViewById(R.id.incValue);
        TextView exp = view.findViewById(R.id.expValue);
        TextView bal = view.findViewById(R.id.balValue);
        TextView badge = view.findViewById(R.id.balBadge);
        ImageView img = view.findViewById(R.id.balImg);

        double e = 0, i = 0;
        if(month != null){
            e = dba.getMonthlyTotExpenses(month.getId());
            i = dba.getMonthlyTotIncomes(month.getId());
        }
        double b = Math.round((i - e) * 100) / 100.0;

        inc.setText(Tools.formatEuro(i));
        exp.setText(Tools.formatEuro(e));
        bal.setText(Tools.formatEuro(b));

        if(b == 0){
            badge.setText("In pari");
            img.setImageResource(R.drawable.ic_trend_flat);
        }else if(b > 0){
            badge.setText("Positivo");
            img.setImageResource(R.drawable.ic_trend_up);
        }else{
            badge.setText("Negativo");
            img.setImageResource(R.drawable.ic_trend_down);
        }
    }

    private void manageButtons(){

        View addButton = view.findViewById(R.id.addBtn);
        View expButton = view.findViewById(R.id.expBtn);

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
            // Let the page (header, Movimenti) know about the month just created
            listner.onChangeMonth(month);
        }
    }

    private View populateView(View view){

        checkMonth();
        populateValues();
        manageButtons();

        return view;
    }
}
