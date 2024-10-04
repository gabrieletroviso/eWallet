package com.example.provajava.gui.activity;

import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;

public interface iActivityManaged {
    void onChangeMonth(TMonth month);
    void onNewTransaction(TTransaction tr);
}

