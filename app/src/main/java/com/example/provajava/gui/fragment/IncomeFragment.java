package com.example.provajava.gui.fragment;

import com.example.provajava.R;
import com.example.provajava.enumerator.eTranSubType;

import java.util.ArrayList;
import java.util.List;

// Statistiche · Entrate: share of Stipendio / Altro over the year's incomes
public class IncomeFragment extends APieChartsFragment {

    @Override
    protected String getTitle() {
        return "Ripartizione entrate";
    }

    @Override
    protected List<Slice> getSlices() {
        long yearId = listner.year.getId();
        List<Slice> list = new ArrayList<>();
        list.add(new Slice(eTranSubType.SALARY.toString(), (float) listner.dba.getAnnualSalaryIncome(yearId),
                R.color.ew_cat_stipendio, R.color.ew_income, R.color.ew_income_container));
        list.add(new Slice(eTranSubType.OTHER_INCOME.toString(), (float) listner.dba.getAnnualOtherIncome(yearId),
                R.color.ew_cat_altro, R.color.ew_income, R.color.ew_income_container));
        return list;
    }
}
