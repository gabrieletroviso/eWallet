package com.example.provajava.gui.fragment.dialog;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.provajava.R;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.gui.CategoryStyle;
import com.example.provajava.gui.activity.TodayActivityPage;
import com.example.provajava.gui.fragment.TodayFragment;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Window to add a new income/expense or edit an existing one:
// amount, date (within the month shown in Home), category and description
public class TransactionDialog extends ManagedDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final eTranSubType[] INCOME_TYPES = {eTranSubType.SALARY, eTranSubType.OTHER_INCOME};
    private static final eTranSubType[] EXPENSE_TYPES = {eTranSubType.NECESSARY, eTranSubType.UNNECESSARY, eTranSubType.EXTRA};

    boolean isIncome;
    DatabaseAccess dba;
    LocalDate chosenDate = null;
    TTransaction transaction = null;

    private View view;
    private final Map<Integer, eTranSubType> chipTypes = new HashMap<>();

    public TransactionDialog(DatabaseAccess dba, boolean isInc, TTransaction trans, iFragmentManaged parent){
        super(parent);
        this.dba = dba;
        this.isIncome = isInc;
        this.transaction = trans;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.transaction_dialog, container, false);

        if (transaction != null) {
            chosenDate = Instant.ofEpochMilli(transaction.getDate()).atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            chosenDate = ((TodayFragment) listner).day;
        }

        setHeader();
        buildCategoryChips(inflater);
        manageDateField();

        if (transaction != null) {
            ((EditText) view.findViewById(R.id.amountValue)).setText(formatAmount(transaction.getAmount()));
            ((EditText) view.findViewById(R.id.descValue)).setText(transaction.getDescription());
        }

        // ManagedDialog builds this view twice (once for the AlertDialog, once as the fragment view):
        // "Salva" must read the fields of the same view it belongs to, never the last one inflated
        final View root = view;
        root.findViewById(R.id.cancelBtn).setOnClickListener(v -> dismiss());
        root.findViewById(R.id.saveBtn).setOnClickListener(v -> save(root));

        return view;
    }

    // ------------------------------------------------------------------------------- layout

    private void setHeader(){
        String what = isIncome ? "entrata" : "uscita";
        ((TextView) view.findViewById(R.id.dialogTitle))
                .setText((transaction == null ? "Nuova " : "Modifica ") + what);

        // Same icon/colors as the Riepilogo cards
        CategoryStyle style = CategoryStyle.of(isIncome ? eTranSubType.OTHER_INCOME : null);
        ImageView icon = view.findViewById(R.id.dialogIcon);
        if (isIncome) {
            style.applyTo(icon);
        } else {
            icon.setImageResource(R.drawable.ic_arrow_out);
            ImageViewCompat.setImageTintList(icon,
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.ew_expense)));
            ViewCompat.setBackgroundTintList(icon,
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.ew_expense_container)));
        }

        MaterialButton save = view.findViewById(R.id.saveBtn);
        save.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), isIncome ? R.color.ew_income : R.color.ew_expense)));
    }

    // One chip per category; the transaction's category (or the first one) is preselected
    private void buildCategoryChips(LayoutInflater inflater){
        ChipGroup group = view.findViewById(R.id.categoryChips);
        eTranSubType current = transaction != null ? transaction.getTranSubType() : null;
        eTranSubType[] types = isIncome ? INCOME_TYPES : EXPENSE_TYPES;

        int toCheck = View.NO_ID;
        for (eTranSubType t : types) {
            Chip chip = (Chip) inflater.inflate(R.layout.category_chip, group, false);
            chip.setId(View.generateViewId());
            chip.setText(t.toString());
            chipTypes.put(chip.getId(), t);
            group.addView(chip);
            if (toCheck == View.NO_ID || t == current) toCheck = chip.getId();
        }
        group.check(toCheck);
    }

    private eTranSubType selectedType(View root){
        ChipGroup group = root.findViewById(R.id.categoryChips);
        eTranSubType t = chipTypes.get(group.getCheckedChipId());
        return t != null ? t : eTranSubType.NONE;
    }

    // ------------------------------------------------------------------------------- date

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void manageDateField(){
        EditText date = view.findViewById(R.id.dateValue);
        date.setText(chosenDate.format(DATE_FORMAT));

        View.OnClickListener open = v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setCalendarConstraints(setCalendarConstraint().build())
                    .setSelection(chosenDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                    .setTitleText("Seleziona una data")
                    .setTheme(R.style.MaterialCalendarTheme)
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                // The picker returns midnight UTC of the chosen day
                chosenDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
                date.setText(chosenDate.format(DATE_FORMAT));
            });
            picker.show(getParentFragmentManager(), "DATE_PICKER");
        };
        date.setOnClickListener(open);
        ((TextInputLayout) view.findViewById(R.id.dateLayout)).setEndIconOnClickListener(open);
    }

    // Only days of the month shown in Home can be picked
    private CalendarConstraints.Builder setCalendarConstraint(){
        TMonth m = getMonth();
        TYear y = dba.getYearById(m.getYearId());
        LocalDate first = LocalDate.of(y.getYear(), m.getMonth(), 1);
        LocalDate last = first.withDayOfMonth(first.lengthOfMonth());

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setStart(first.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        constraintsBuilder.setEnd(last.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        constraintsBuilder.setOpenAt(chosenDate.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        return constraintsBuilder;
    }

    private TMonth getMonth(){
        TodayActivityPage todPage = (TodayActivityPage) listner.getParentPage();
        return todPage.getMonth();
    }

    // ------------------------------------------------------------------------------- save

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void save(View root){
        TextInputLayout amountLayout = root.findViewById(R.id.amountLayout);
        String raw = ((EditText) root.findViewById(R.id.amountValue)).getText().toString().trim().replace(',', '.');
        String desc = ((EditText) root.findViewById(R.id.descValue)).getText().toString().trim();
        eTranSubType type = selectedType(root);

        double amount;
        try {
            amount = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            amountLayout.setError("Inserisci un importo");
            return;
        }
        if (amount <= 0) {
            amountLayout.setError("L'importo deve essere maggiore di zero");
            return;
        }
        amountLayout.setError(null);
        if (eTranSubType.NONE.equals(type)) {
            Toast.makeText(getActivity(), "Scegli una categoria", Toast.LENGTH_SHORT).show();
            return;
        }

        TTransaction t;
        if (transaction != null) {
            transaction.setAmount(amount);
            transaction.setDescription(desc);
            transaction.setTranSubType(type);
            transaction.setDate(chosenDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            dba.updateTransaction(transaction);
            t = transaction;
        } else {
            t = dba.addTransaction(chosenDate, amount, type, desc);
        }

        onNewTransaction(t);
        dismissFragment();
    }

    // 12.5 → "12,50", 30.0 → "30"
    private static String formatAmount(double v){
        DecimalFormat f = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ITALY));
        String s = f.format(v);
        if (s.contains(",") && s.indexOf(',') == s.length() - 2) s += "0";
        return s;
    }
}
