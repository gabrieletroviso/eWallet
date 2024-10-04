package com.example.provajava.gui.bottomsheet;

import com.example.provajava.datamodel.TYear;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

public class BottomSheetYear extends ABottomSheet{

    public BottomSheetYear(iFragmentManaged listner, DatabaseAccess dba) {
        super(listner, dba);
        items = dba.getAllYears();
    }

    @Override
    protected void onElementClick(int position) {
        TYear clickedItem = (TYear) items.get(position);
        if (listner != null) {
            listner.onFragmentClick(clickedItem.getId());
        }
        dismiss();
    }
}
