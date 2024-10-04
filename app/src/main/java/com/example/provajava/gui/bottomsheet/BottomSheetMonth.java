package com.example.provajava.gui.bottomsheet;

import com.example.provajava.datamodel.TMonth;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

public class BottomSheetMonth extends ABottomSheet {

    public BottomSheetMonth(iFragmentManaged listner, DatabaseAccess dba) {
        super(listner, dba);
        items = dba.getOrderedMonths();
    }

    @Override
    protected void onElementClick(int position) {
        TMonth clickedItem = (TMonth) items.get(position);
        if (listner != null) {
            listner.onFragmentClick(clickedItem.getId());
        }
        dismiss();
    }

}
