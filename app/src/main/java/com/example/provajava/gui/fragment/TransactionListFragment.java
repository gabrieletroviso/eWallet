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
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.enumerator.eOrder;
import com.example.provajava.enumerator.eTranMainType;
import com.example.provajava.gui.activity.TodayActivityPage;
import com.example.provajava.gui.fragment.dialog.TransactionDialog;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.github.mikephil.charting.charts.ScatterChart;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TransactionListFragment extends Fragment implements iFragmentManaged {

    private TodayActivityPage listner;
    private DatabaseAccess dba;
    private List<TTransaction> trss;
    private TMonth month;

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

        View view = inflater.inflate(R.layout.todaylist, container, false);
        view = populateView(view);
        return view;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createOrUpdateView(){
        View view = getView();
        populateView(view);
    }

    public void updateMonth(TMonth m){
        this.month = m;
    }

    public void setListner(TodayActivityPage listner){
        this.listner = listner;
        this.month = listner.getMonth();
        this.dba = listner.getDba();
    }

    private View populateView(View view){

        TextView header = view.findViewById(R.id.todTrnHeader);
        TextView yearHdr = view.findViewById(R.id.todTrnHeaderYear);

        ConstraintLayout cstLy = view.findViewById(R.id.innerConstraintLayout);
        TableLayout tableLayout = view.findViewById(R.id.tableLayout);

        trss = dba.getTransactionsByMonthID(month.getId(), eOrder.DATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            header.setText(Month.of(month.getMonth()).getDisplayName(TextStyle.FULL, Locale.ITALIAN));
            yearHdr.setText(String.valueOf(dba.getYearById(month.getYearId()).getYear()));
        }

        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        for (TTransaction t : trss) {
            TableRow tableRow = getTableRow(t, tableLayout, cstLy);
            tableLayout.addView(tableRow);
        }

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private TableRow getTableRow(TTransaction tran, TableLayout tableLayout, ConstraintLayout cstLy){

        TableRow tableRow = (TableRow) LayoutInflater.from(getContext())
                .inflate(R.layout.trnitem, tableLayout, false);
        tableRow.setTag(tran);

        TextView day = tableRow.findViewById(R.id.day);
        TextView subtype = tableRow.findViewById(R.id.subtype);
        TextView value = tableRow.findViewById(R.id.value);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            day.setText(String.valueOf(Instant.ofEpochMilli(tran.getDate())
                    .atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth()));
        }
        subtype.setText(tran.getTranSubType().toString());
        value.setText(Tools.roundToPrint(tran.getAmount()));

        if (tran.getTranMainType().equals(eTranMainType.EXPENSE)) {
            value.setText(String.valueOf(tran.getAmount()*-1));
            value.setTextColor(getResources().getColor(R.color.dark_red, null));
        }else{
            value.setTextColor(getResources().getColor(R.color.dark_green, null));
        }

        tableRow.setOnClickListener(view -> showPopupWindow(tableRow.getTag(), cstLy));

        return tableRow;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showPopupWindow(Object tag, ConstraintLayout cstLy) {

        TTransaction trns = (TTransaction) tag;
        String desc = trns.getDescription();

        LayoutInflater inflater = (LayoutInflater) listner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.transactionpopup, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int width = popupView.getMeasuredWidth();
        int height = popupView.getMeasuredHeight();

        int[] location = new int[2];
        cstLy.getLocationOnScreen(location);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;

        TextView dataText = popupView.findViewById(R.id.dataTrnsListText);
        ImageButton mod = popupView.findViewById(R.id.editBtn);
        ImageButton del = popupView.findViewById(R.id.deleteBtn);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        dataText.setText(Instant.ofEpochMilli(trns.getDate()).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter));

        TextView descText = popupView.findViewById(R.id.editTextTextMultiLine);
        descText.setText(desc != null && !desc.isEmpty() ? desc : "");

        popupWindow.showAtLocation(getActivity().findViewById(android.R.id.content), Gravity.NO_GRAVITY, x, y);
        popupView.setOnClickListener(v -> popupWindow.dismiss());

        mod.setOnClickListener(v -> {
            DialogFragment dialog = new TransactionDialog(
                    dba, trns.getTranMainType().equals(eTranMainType.INCOME) ? true : false,
                    trns, this);
            dialog.show(getParentFragmentManager(), "Income");
        });

        del.setOnClickListener(v -> {
            dba.deleteTransaction(trns);
            listner.onNewTransaction(null);
            popupWindow.dismiss();
        });
    }
}
