package com.example.provajava.gui.fragment;

import com.example.provajava.R;
import com.example.provajava.enumerator.eTranSubType;

import java.util.ArrayList;
import java.util.List;

// Statistiche · Spese: share of Necessaria / Superflua / Extra over the year's expenses
public class ExpensesFragment extends APieChartsFragment {

    @Override
    protected String getTitle() {
        return "Ripartizione spese";
    }

    @Override
    protected List<Slice> getSlices() {
        List<Slice> list = new ArrayList<>();
        list.add(new Slice(eTranSubType.NECESSARY.toString(), (float) listner.year.getNecExpenses(),
                R.color.ew_cat_necessaria, R.color.ew_cat_necessaria_icon, R.color.ew_cat_necessaria_container));
        list.add(new Slice(eTranSubType.UNNECESSARY.toString(), (float) listner.year.getUnnecExpenses(),
                R.color.ew_cat_superflua, R.color.ew_cat_superflua_icon, R.color.ew_cat_superflua_container));
        list.add(new Slice(eTranSubType.EXTRA.toString(), (float) listner.year.getExtraExpenses(),
                R.color.ew_cat_extra, R.color.ew_cat_extra_icon, R.color.ew_cat_extra_container));
        return list;
    }
}
