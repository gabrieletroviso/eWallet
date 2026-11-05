package com.example.provajava.gui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.provajava.R;
import com.example.provajava.Tools;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.MovementList;
import com.example.provajava.gui.activity.SearchActivityPage;
import com.example.provajava.gui.fragment.interfaces.iFragmentManaged;

import java.util.List;

// Ricerca · Risultati: summary of the search and the transactions found, grouped by day
public class TransactionSearchFragment extends Fragment implements iFragmentManaged {

    private SearchActivityPage listner;
    private DatabaseAccess dba;
    private View view;

    @Override
    public void onFragmentDismissed() {}

    @Override
    public void onFragmentClick(long id) {}

    @Override
    public void onFragmentNewTransaction(TTransaction tr) {
        listner.onNewTransaction(tr);
    }

    @Override
    public AppCompatActivity getParentPage() {
        return listner;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.searchlist, container, false);
        // A search may have been run before this page was created
        if (listner.getLastResults() != null) {
            populateTransaction(listner.getLastResults());
        }
        return view;
    }

    public void setListner(SearchActivityPage listner){
        this.listner = listner;
        this.dba = listner.getDba();
    }

    public void populateTransaction(List<TTransaction> trss){

        View scroll = view.findViewById(R.id.resultsScroll);
        TextView empty = view.findViewById(R.id.resultsEmpty);

        if (trss.isEmpty()) {
            scroll.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            empty.setText("Nessun movimento trovato.\nProva a cambiare i filtri.");
            return;
        }

        double income = 0, expense = 0;
        for (TTransaction t : trss) {
            double v = MovementList.signedAmount(t);
            if (v >= 0) income += v; else expense -= v;
        }
        double balance = income - expense;

        ((TextView) view.findViewById(R.id.resultsCount)).setText(
                trss.size() == 1 ? "1 movimento trovato" : trss.size() + " movimenti trovati");
        ((TextView) view.findViewById(R.id.resultsIncome)).setText(Tools.formatEuro(income));
        ((TextView) view.findViewById(R.id.resultsExpense)).setText(Tools.formatEuro(expense));
        TextView bal = view.findViewById(R.id.resultsBalance);
        bal.setText(Tools.formatEuro(balance, true));
        bal.setTextColor(ContextCompat.getColor(requireContext(),
                Math.round(balance * 100) < 0 ? R.color.ew_expense : R.color.ew_income));

        // Results can span several months/years: show the year in the day labels
        MovementList.render((LinearLayout) view.findViewById(R.id.resultsContainer), trss, true, null);

        empty.setVisibility(View.GONE);
        scroll.setVisibility(View.VISIBLE);
        scroll.scrollTo(0, 0);
    }
}
