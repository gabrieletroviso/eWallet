package com.example.provajava.gui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.gui.Notice;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.gui.activity.SearchActivityPage;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Ricerca · Filtri: period, amount range, description and transaction types
public class TransactionFilterFragment extends Fragment implements iFragmentManaged {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Chip id → transaction type it filters on
    private static final int[] CHIP_IDS = {R.id.salChip, R.id.othChip, R.id.necChip, R.id.unnChip, R.id.extChip};
    private static final eTranSubType[] CHIP_TYPES = {eTranSubType.SALARY, eTranSubType.OTHER_INCOME,
            eTranSubType.NECESSARY, eTranSubType.UNNECESSARY, eTranSubType.EXTRA};

    private SearchActivityPage listner;
    DatabaseAccess dba;
    private View view;
    private LocalDate from = LocalDate.now();
    private LocalDate to = LocalDate.now();

    @Override
    public void onFragmentDismissed() {}

    @Override
    public void onFragmentClick(long id) {}

    @Override
    public void onFragmentNewTransaction(TTransaction tr) {}

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    public void setListner(SearchActivityPage listner){
        this.listner = listner;
        this.dba = listner.dba;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.searchfilter, container, false);

        setDateField(R.id.fromLayout, R.id.fromBtn, true);
        setDateField(R.id.toLayout, R.id.toBtn, false);
        showDates();

        view.findViewById(R.id.resetBtn).setOnClickListener(v -> reset());
        view.findViewById(R.id.searchBtn).setOnClickListener(v -> search());

        return view;
    }

    // ------------------------------------------------------------------------------- dates

    private void setDateField(int layoutId, int editId, boolean isFrom) {
        View.OnClickListener open = v -> {
            LocalDate current = isFrom ? from : to;
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(isFrom ? "Dal" : "Al")
                    .setSelection(current.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                    .setTheme(R.style.MaterialCalendarTheme)
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                // The picker returns midnight UTC of the chosen day
                LocalDate chosen = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
                if (isFrom) from = chosen; else to = chosen;
                showDates();
            });
            picker.show(getParentFragmentManager(), "DATE_PICKER");
        };
        view.findViewById(editId).setOnClickListener(open);
        ((TextInputLayout) view.findViewById(layoutId)).setEndIconOnClickListener(open);
    }

    private void showDates() {
        ((EditText) view.findViewById(R.id.fromBtn)).setText(from.format(DATE_FORMAT));
        ((EditText) view.findViewById(R.id.toBtn)).setText(to.format(DATE_FORMAT));
    }

    // ------------------------------------------------------------------------------ actions

    private void reset() {
        from = LocalDate.now();
        to = LocalDate.now();
        showDates();
        ((EditText) view.findViewById(R.id.moreVal)).setText("");
        ((EditText) view.findViewById(R.id.lessVal)).setText("");
        ((EditText) view.findViewById(R.id.descVal)).setText("");
        for (int id : CHIP_IDS) {
            ((Chip) view.findViewById(id)).setChecked(false);
        }
    }

    private void search() {
        Double more, less;
        try {
            more = parseAmount(R.id.moreVal);
            less = parseAmount(R.id.lessVal);
        } catch (NumberFormatException e) {
            toast("Importo non valido");
            return;
        }

        if (from.isAfter(to)) {
            toast("La data \"Dal\" deve essere precedente o uguale alla data \"Al\"");
            return;
        }
        if (more != null && less != null && more > less) {
            toast("\"Maggiore di\" deve essere minore o uguale a \"Minore di\"");
            return;
        }

        List<eTranSubType> types = new ArrayList<>();
        for (int i = 0; i < CHIP_IDS.length; i++) {
            if (((Chip) view.findViewById(CHIP_IDS[i])).isChecked()) types.add(CHIP_TYPES[i]);
        }
        String desc = ((EditText) view.findViewById(R.id.descVal)).getText().toString().trim();

        // Whole days: from the start of "Dal" to the end of "Al"
        long start = from.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;

        List<TTransaction> trss = dba.getFilteredTransactionsV2(types.isEmpty() ? null : types,
                start, end, more, less, desc.isEmpty() ? null : desc);

        hideKeyboard();
        listner.populateTransaction(trss != null ? trss : new ArrayList<>());
    }

    // Accepts both "12,50" and "12.50"; empty means "no limit"
    private Double parseAmount(int editId) {
        String s = ((EditText) view.findViewById(editId)).getText().toString().trim().replace(',', '.');
        return s.isEmpty() ? null : Double.valueOf(s);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    private void toast(String msg) {
        Notice.error(getActivity(), msg);
    }
}
