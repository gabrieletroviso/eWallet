package com.example.provajava.gui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.example.provajava.gui.MovementList;
import com.example.provajava.gui.activity.TodayActivityPage;
import com.example.provajava.gui.fragment.dialog.TransactionDialog;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

        trss = month == null ? new ArrayList<>() : dba.getTransactionsByMonthID(month.getId(), eOrder.DATE);
        empty.setVisibility(trss.isEmpty() ? View.VISIBLE : View.GONE);
        MovementList.render(container, trss, false, this::showPopupWindow);

        return view;
    }

    // Detail of a transaction in a panel from the bottom: category, amount, date, description,
    // Modifica (opens the edit window) and Elimina (asks for confirmation)
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showPopupWindow(TTransaction trns) {

        BottomSheetDialog sheet = new BottomSheetDialog(requireContext(), R.style.ThemeOverlay_eWallet_BottomSheet);
        View content = LayoutInflater.from(sheet.getContext()).inflate(R.layout.transaction_sheet, null, false);
        sheet.setContentView(content);

        boolean isIncome = trns.getTranMainType().equals(eTranMainType.INCOME);
        LocalDate date = Instant.ofEpochMilli(trns.getDate()).atZone(ZoneId.systemDefault()).toLocalDate();

        CategoryStyle.of(trns.getTranSubType()).applyTo(content.findViewById(R.id.sheetIcon));
        ((TextView) content.findViewById(R.id.sheetCategory)).setText(
                trns.getTranSubType() + " · " + (isIncome ? "entrata" : "uscita"));
        String day = date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN));
        ((TextView) content.findViewById(R.id.sheetDate)).setText(
                day.substring(0, 1).toUpperCase(Locale.ITALIAN) + day.substring(1));

        TextView amount = content.findViewById(R.id.sheetAmount);
        amount.setText(Tools.formatEuro(MovementList.signedAmount(trns), true));
        amount.setTextColor(ContextCompat.getColor(requireContext(), isIncome ? R.color.ew_income : R.color.ew_text));

        TextView descText = content.findViewById(R.id.sheetDescription);
        String desc = trns.getDescription();
        boolean hasDesc = desc != null && !desc.trim().isEmpty();
        descText.setText(hasDesc ? desc.trim() : "Nessuna descrizione");
        descText.setTextColor(ContextCompat.getColor(requireContext(),
                hasDesc ? R.color.ew_text : R.color.ew_text_secondary));

        content.findViewById(R.id.editBtn).setOnClickListener(v -> {
            sheet.dismiss();
            DialogFragment dialog = new TransactionDialog(dba, isIncome, trns, this);
            dialog.show(getParentFragmentManager(), isIncome ? "Income" : "Expense");
        });

        content.findViewById(R.id.deleteBtn).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Eliminare il movimento?")
                        .setMessage(trns.getTranSubType() + " di " + Tools.formatEuro(trns.getAmount())
                                + " del " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".")
                        .setNegativeButton("Annulla", null)
                        .setPositiveButton("Elimina", (d, w) -> {
                            dba.deleteTransaction(trns);
                            sheet.dismiss();
                            listner.onNewTransaction(null);
                        })
                        .show());

        sheet.show();
    }
}
