package com.example.provajava.datamodel;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.Entity;

@Entity(tableName = "T_Year")
public class TYear {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="ID")
    private long id;
    @ColumnInfo(name="Year")
    private int year;
    @ColumnInfo(name="MeanIncome")
    private double meanIncome;
    @ColumnInfo(name="AnnualIncomeProjection")
    private double incomeProjection;
    @ColumnInfo(name="MeanBalance")
    private double meanBalance;
    @ColumnInfo(name="AnnualBalanceProjection")
    private double balanceProjection;
    @ColumnInfo(name="MeanExpenses")
    private double meanExpenses;
    @ColumnInfo(name="MeanNecExpenses")
    private double meanNecExpenses;
    @ColumnInfo(name="MeanUnnecExpenses")
    private double meanUnnecExpenses;
    @ColumnInfo(name="MeanExtraExpenses")
    private double meanExtraExpenses;
    @ColumnInfo(name="Expenses")
    private double expenses;
    @ColumnInfo(name="NecExpenses")
    private double necExpenses;
    @ColumnInfo(name="UnnecExpenses")
    private double unnecExpenses;
    @ColumnInfo(name="ExtraExpenses")
    private double extraExpenses;
    @ColumnInfo(name="YearTrade")
    private double yearTrade;
    @ColumnInfo(name="YearEndTradePosition")
    private Double endPos;
        
    public TYear(){
        year = 0;
        meanIncome = 0;
        incomeProjection = 0;
        meanBalance = 0;
        balanceProjection = 0;
        meanExpenses = 0;
        meanNecExpenses = 0;
        meanUnnecExpenses = 0;
        meanExtraExpenses = 0;
        expenses = 0;
        necExpenses = 0;
        unnecExpenses = 0;
        extraExpenses = 0;
        yearTrade = 0;
        endPos = null;
    }
    
    public long getId(){
        return id;
    }
    
    public void setId(long id){
        this.id = id;
    }
    
    public int getYear(){
        return year;
    }
    
    public void setYear(int y){
        if(y<0){
            throw new IllegalArgumentException("Year must be positive");
        }else{
            this.year = y;
        }
    }
    
    public double getMeanIncome(){
        return meanIncome;
    }
    
    public void setMeanIncome(double mean){
        this.meanIncome = mean;
    }
    
    public double getIncomeProjection(){
        return incomeProjection;
    }
    
    public void setIncomeProjection(double proj){
        this.incomeProjection = proj;
    }
    
    public double getMeanBalance(){
        return meanBalance;
    }
    
    public void setMeanBalance(double mean){
        this.meanBalance = mean;
    }
    
    public double getBalanceProjection(){
        return balanceProjection;
    }
    
    public void setBalanceProjection(double proj){
        this.balanceProjection = proj;
    }
    
    public double getMeanExpenses(){
        return meanExpenses;
    }
    
    public void setMeanExpenses(double mean){
        this.meanExpenses = mean;
    }
    
    public double getMeanUnnecExpenses(){
        return meanUnnecExpenses;
    }
    
    public void setMeanUnnecExpenses(double mean){
        this.meanUnnecExpenses = mean;
    }
    
    public double getMeanNecExpenses(){
        return meanNecExpenses;
    }
    
    public void setMeanNecExpenses(double mean){
        this.meanNecExpenses = mean;
    }
    
    public double getMeanExtraExpenses(){
        return meanExtraExpenses;
    }
    
    public void setMeanExtraExpenses(double mean){
        this.meanExtraExpenses = mean;
    }
    
    public double getExpenses(){
        return expenses;
    }
    
    public void setExpenses(double exp){
        this.expenses = exp;
    }
    
    public double getUnnecExpenses(){
        return unnecExpenses;
    }
    
    public void setUnnecExpenses(double exp){
        this.unnecExpenses = exp;
    }
    
    public double getNecExpenses(){
        return necExpenses;
    }
    
    public void setNecExpenses(double exp){
        this.necExpenses = exp;
    }
    
    public double getExtraExpenses(){
        return extraExpenses;
    }
    
    public void setExtraExpenses(double exp){
        this.extraExpenses = exp;
    }
    
    public double getYearTrade(){
        return yearTrade;
    }
    
    public void setYearTrade(double t){
        this.yearTrade = t;
    }
    
    public Double getEndPos(){
        return endPos;
    }
    
    public void setEndPos(Double pos){
        this.endPos = pos;
    }
}
