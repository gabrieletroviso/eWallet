package com.example.provajava.gui.fragment.interfaces;

import androidx.appcompat.app.AppCompatActivity;

import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;

// Use this interface to manage fragment dismiss callback
public interface iFragmentManaged {

    void onFragmentDismissed();
    void onFragmentClick(long id);
    void onFragmentNewTransaction(TTransaction tr);
    AppCompatActivity getParentPage();

}
