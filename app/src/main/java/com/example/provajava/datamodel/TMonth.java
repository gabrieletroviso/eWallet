package com.example.provajava.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Entity;

@Entity(
    tableName = "T_Month",
    foreignKeys = @ForeignKey(
        entity = TYear.class,
        parentColumns = "ID",
        childColumns = "Year_ID",
        onDelete = ForeignKey.CASCADE
    )
)
public class TMonth implements Parcelable {

    private static Parcelable.Creator CREATOR;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="ID")
    private long id;
    @ColumnInfo(name="Month")
    private int month;
    @ColumnInfo(name="Year_ID")
    private long yearId;
    @ColumnInfo(name="TotIncome")
    private double totIncome;
    @ColumnInfo(name="TotOthIncome")
    private double totOthIncome;
    @ColumnInfo(name="TotExpenses")
    private double totExpenses;
    @ColumnInfo(name="TotUnnecExpenses")
    private double totUnnecExpenses;
    @ColumnInfo(name="TotNecExpenses")
    private double totNecExpenses;
    @ColumnInfo(name="TotExtraExpenses")
    private double totExtraExpenses;
    @ColumnInfo(name="Balance")
    private double balance;
    @ColumnInfo(name="Trade")
    private double trade;
    @ColumnInfo(name="EndPosition")
    private double endPosition;
    
    public TMonth(){
        month = 0;
        yearId = 0;
        totIncome = 0;
        totOthIncome = 0;
        totExpenses = 0;
        totNecExpenses = 0;
        totUnnecExpenses = 0;
        totExtraExpenses = 0;
        balance = 0;
        trade = 0;
        endPosition = 0;
    }
    
    public long getId(){
        return id;
    }
    
    public void setId(long id){
        this.id = id;
    }
    
    public long getYearId(){
        return yearId;
    }
    
    public void setYearId(long id){
        yearId = id;
    }
    
    public int getMonth(){
        return month;
    }
    
    public void setMonth(int month){
        if(month>12 || month<1){
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }else{
            this.month = month;
        }
    }
    
    public double getTotIncome(){
        return totIncome;
    }
    
    public void setTotIncome(double tot){
        this.totIncome = tot;
    }
    
    public double getTotOthIncome(){
        return totOthIncome;
    }
    
    public void setTotOthIncome(double tot){
        this.totOthIncome = tot;
    }
    
    public double getTotExpenses(){
        return totExpenses;
    }
    
    public void setTotExpenses(double tot){
        this.totExpenses = tot;
    }
    
    public double getTotExtraExpenses(){
        return totExtraExpenses;
    }
    
    public void setTotExtraExpenses(double tot){
        this.totExtraExpenses = tot;
    }
    
    public double getTotUnnecExpenses(){
        return totUnnecExpenses;
    }
    
    public void setTotUnnecExpenses(double tot){
        this.totUnnecExpenses = tot;
    }
    
    public double getTotNecExpenses(){
        return totNecExpenses;
    }
    
    public void setTotNecExpenses(double tot){
        this.totNecExpenses = tot;
    }
    
    public double getBalance(){
        return balance;
    }
    
    public void setBalance(double bal){
        this.balance = bal;
    }
    
    public double getTrade(){
        return trade;
    }
    
    public void setTrade(double t){
        this.trade = t;
    }
    
    public double getEndPosition(){
        return endPosition;
    }
    
    public void setEndPosition(double end){
        this.endPosition = end;
    }
    
    public void incrementTotOthIncome(double i){
        this.totOthIncome+=i;
        incrementTotIncome(i);
    }
    
    public void incrementTotIncome(double i){
        this.totIncome+=i;
        this.balance+=i;
    }
    
    public void incrementTotNecExp(double a){
        this.totNecExpenses += a;
        incrementTotExp(a);
    }
    
    public void incrementTotExtraExp(double a){
        this.totExtraExpenses += a;
        incrementTotExp(a);
    }
    
    public void incrementTotUnnExp(double a){
        this.totUnnecExpenses += a;
        incrementTotExp(a);
    }
    
    public void incrementTrade(double a){
        this.trade+=a;
    }
    
    private void incrementTotExp(double a){
        this.totExpenses += a;
        this.balance -=a;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {

    }
}
