package com.example.provajava.gui.bottomsheet;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.provajava.R;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.dbmanager.DatabaseAccess;
import com.example.provajava.gui.fragment.interfaces.iOnItemClickListner;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class BottomSheetElement extends RecyclerView.Adapter<BottomSheetElement.ViewHolder> {

    private List items;
    private Context context;
    private iOnItemClickListner onItemClickListener;
    private DatabaseAccess dba;
    private boolean isMonth;

    public BottomSheetElement(Context context, List items, DatabaseAccess dba, iOnItemClickListner listener) {
        this.context = context;
        this.items = items;
        this.onItemClickListener = listener;
        this.dba = dba;

        setIsMonth();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = createTexView();
        return new ViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(isMonth){
            TMonth month = (TMonth) items.get(position);
            TYear year = dba.getYearById(month.getYearId());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                holder.textView.setText(
                        Month.of(month.getMonth()).getDisplayName(TextStyle.FULL, Locale.ITALIAN)+
                                " " + year.getYear()
                );
            }
        }else{
            TYear year = (TYear) items.get(position);
            holder.textView.setText(String.valueOf(year.getYear()));
        }

        holder.textView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

    // Create element list
    private TextView createTexView(){

        Typeface typeface = ResourcesCompat.getFont(context, R.font.nunito);

        TextView textView = new TextView(context);
        textView.setPadding(20, 50, 20, 50);
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setClickable(true);
        textView.setFocusable(true);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        int backgroundResource = typedValue.resourceId;

        textView.setBackgroundResource(backgroundResource);
        textView.setTextColor(ContextCompat.getColor(context, R.color.apptext));

        return textView;
    }

    // Set if it's month or not
    private void setIsMonth(){
        if(items.get(0) instanceof TMonth){
            isMonth = true;
        }else{
            isMonth = false;
        }
    }
}