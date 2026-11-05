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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.enumerator.eOrder;
import com.example.provajava.enumerator.eTranMainType;
import com.example.provajava.gui.CategoryStyle;
import com.example.provajava.gui.activity.TodayActivityPage;
import com.example.provajava.gui.fragment.dialog.TransactionDialog;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    // Build the list: one section per day (header with the day's total + card with its rows)
    private View populateView(View view){

        LinearLayout container = view.findViewById(R.id.dayContainer);
        TextView empty = view.findViewById(R.id.emptyText);
        container.removeAllViews();

        trss = month == null ? new ArrayList<>() : dba.getTransactionsByMonthID(month.getId(), eOrder.DATE);
        empty.setVisibility(trss.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        LocalDate currentDay = null;
        LinearLayout rows = null;
        TextView dayTotal = null;
        double total = 0;

        for (TTransaction t : trss) {
            LocalDate date = Instant.ofEpochMilli(t.getDate()).atZone(ZoneId.systemDefault()).toLocalDate();

            if (!date.equals(currentDay)) {
                if (dayTotal != null) dayTotal.setText(Tools.formatEuro(total, true));
                View section = inflater.inflate(R.layout.movement_day, container, false);
                ((TextView) section.findViewById(R.id.dayLabel)).setText(dayLabel(date));
                dayTotal = section.findViewById(R.id.dayTotal);
                rows = section.findViewById(R.id.dayRows);
                container.addView(section);
                currentDay = date;
                total = 0;
            }

            total += signedAmount(t);
            rows.addView(buildRow(t, inflater, rows));
        }
        if (dayTotal != null) dayTotal.setText(Tools.formatEuro(total, true));

        return view;
    }

    private View buildRow(TTransaction tran, LayoutInflater inflater, ViewGroup parent){

        View row = inflater.inflate(R.layout.movement_row, parent, false);

        CategoryStyle.of(tran.getTranSubType()).applyTo(row.findViewById(R.id.rowIcon));
        ((TextView) row.findViewById(R.id.rowCategory)).setText(tran.getTranSubType().toString());

        TextView desc = row.findViewById(R.id.rowDescription);
        String d = tran.getDescription();
        if (d != null && !d.trim().isEmpty()) {
            desc.setText(d.trim());
            desc.setVisibility(View.VISIBLE);
        }

        TextView amount = row.findViewById(R.id.rowAmount);
        amount.setText(Tools.formatEuro(signedAmount(tran), true));
        amount.setTextColor(ContextCompat.getColor(requireContext(),
                tran.getTranMainType().equals(eTranMainType.EXPENSE) ? R.color.ew_text : R.color.ew_income));

        row.setOnClickListener(v -> showPopupWindow(tran));
        return row;
    }

    // Expenses are stored as positive amounts: show them as negative
    private static double signedAmount(TTransaction t){
        return t.getTranMainType().equals(eTranMainType.EXPENSE) ? -t.getAmount() : t.getAmount();
    }

    // "Domenica 1 novembre"
    private static String dayLabel(LocalDate date){
        String dow = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        return dow.substring(0, 1).toUpperCase(Locale.ITALIAN) + dow.substring(1)
                + " " + date.getDayOfMonth() + " " + month;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showPopupWindow(TTransaction trns) {

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
            popupWindow.dismiss();
        });

        del.setOnClickListener(v -> {
            dba.deleteTransaction(trns);
            listner.onNewTransaction(null);
            popupWindow.dismiss();
        });
    }
}
