package com.example.provajava.dbmanager;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.provajava.querydao.*;
import com.example.provajava.datamodel.*;
import com.example.provajava.enumerator.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class Engine {

    private final TTransactionDAO tDao;
    private final TDayDAO dDao;
    private final TMonthDAO mDao;
    private final TYearDAO yDao;
    
    public Engine(Context ctx){

        Database db = Database.getInstance(ctx);
        tDao = db.getTransactionDAO();
        dDao = db.getDayDAO();
        mDao = db.getMonthDAO();
        yDao = db.getYearDAO();
    }

    // Get year dm
    public TYear getYear(int year){
        return yDao.getYearDMByYear(year);
    }

    // Get year dm
    public TYear getYearById(long id){
        return yDao.getYearByID(id);
    }

    // Get all years dm
    public List<TYear> getYears(){
        return yDao.getAllYears();
    }

    // Get month dm
    public TMonth getMonth(int m, int y){
        return mDao.getMonthDMByMonthAndYear(m, y);
    }

    // Get month dm by id
    public TMonth getMonthById(long id){
        return mDao.getMonthByID(id);
    }

    // Get all month dm
    public List<TMonth> getOrderedMonths(){
        return mDao.getAllOrderedMonths();
    }

    // Get all month by year id
    public List<TMonth> getMonthsByYearId(long id){
        return mDao.getMonthsByYearID(id);
    }

    // Get all transaction by month id and ordered by type
    public List<TTransaction> getTransactionsByMonthIDOrderedByType(long id){
        return tDao.getTransactionsByMonthIDOrderedByType(id);
    }

    // Get all transaction by month id and ordered by subtype
    public List<TTransaction> getTransactionsByMonthIDOrderedBySubType(long id){
        return tDao.getTransactionsByMonthIDOrderedBySubType(id);
    }

    // Get all transaction by month id and ordered by date
    public List<TTransaction> getTransactionsByMonthIDOrderedByDate(long id){
        return tDao.getTransactionsByMonthIDOrderedByDate(id);
    }

    // Get all transaction by month id and ordered by value
    public List<TTransaction> getTransactionsByMonthIDOrderedByValue(long id){
        return getTransactionsByMonthIDOrderedByValue(id);
    }

    //Get day dm
    public TDay getDay(int d, int m, int y){
        return dDao.getDayFromDateValues(d, m, y);
    }

    public void updateTransaction(TTransaction tran){

        TMonth month = mDao.getMonthByID(tran.getMonthId());
        TYear year = yDao.getYearByID(tran.getYearId());

        tDao.update(tran);
        updateMonth(year, month, tran);

    }

    // Add transaction record with comment and manage month and year statistics
    @RequiresApi(api = Build.VERSION_CODES.O)
    public TTransaction addTransaction(LocalDate date, double amount, eTranSubType sub, String desc){
        
        long msDate = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        TTransaction trns = new TTransaction();
        trns.setDate(msDate);
        trns.setAmount(amount);
        trns.setTranSubType(sub);
        trns.setDescription(desc);
        
        TYear year = findOrCreateYear(date.getYear());
        TMonth month = findOrCreateMonth(date.getMonthValue(), year.getId());
        TDay day = findOrCreateDay(date.getDayOfMonth(), month.getId(), year.getId());
        
        trns.setYearId(year.getId());
        trns.setMonthId(month.getId());
        trns.setDayId(day.getId());
        
        updateMonth(year, month, trns);

        trns.setId(tDao.insert(trns));
        return trns;
    }
    
    // Add monthly final position. If month is december, 
    // this method will update year end position also
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addMonthlyFinalPos(LocalDate date, double endPos){
        
        int month = date.getMonthValue();
        int year = date.getYear();
        
        long mid = mDao.getMonthIDByMonthAndYear(month, year);
        mDao.updateEndPosition(endPos, mid);
        
        if(month==12) yDao.updateEndTradePos(endPos, yDao.getYearIDByYear(year));
    }
    
    // Calculate expenses percentages between two dates
    @RequiresApi(api = Build.VERSION_CODES.O)
    public double[] getExpensesStatsBetween(LocalDate start, LocalDate end){

        long mstart = start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long mend = end.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        double nec = tDao.getExpensesBetween(mstart, mend, eTranSubType.NECESSARY);
        double unn = tDao.getExpensesBetween(mstart, mend, eTranSubType.UNNECESSARY);
        double ext = tDao.getExpensesBetween(mstart, mend, eTranSubType.EXTRA);
        double tot = nec+unn+ext;
        
        double[] res = {nec/tot, unn/tot, ext/tot};
        return res;
    }

    // Return statistics about expenses between day 1 and day 10
    // on all months of the year
    public double[] getAnnualHorizontalFirstPeriod(int year){
        return getPeriodExpensesAnnualMean(year, 1, 10);
    }
    
    // Return statistics about expenses between day 11 and day 20
    // on all months of the year
    public double[] getAnnualHorizontalSecondPeriod(int year){
        return getPeriodExpensesAnnualMean(year, 11, 20);
    }
    
    // Return statistics about expenses between day 21 and day 31
    // on all months of the year
    public double[] getAnnualHorizontalThirdPeriod(int year){
        return getPeriodExpensesAnnualMean(year, 21, 31);
    }

    // Get total monthly income
    public double getMonthlyTotIncomes(long monthId){
        return tDao.getTotalAmountFromMainTypeAndMonthID(eTranMainType.INCOME, monthId);
    }

    // Get total monthly expenses
    public double getMonthlyTotExpenses(long monthId){
        return tDao.getTotalAmountFromMainTypeAndMonthID(eTranMainType.EXPENSE, monthId);
    }

    // Get incomes greater than input amount
    public List<TTransaction> getIncomesGreaterThan(double amount) {
        return tDao.getIncomesGreaterThan(eTranMainType.INCOME, amount);
    };

    // Get incomes less than input amount
    public List<TTransaction> getIncomesLessThan(double amount) {
        return tDao.getIncomesLessThan(eTranMainType.INCOME, amount);
    };

    // Get annual income by subtype
    public double getIncomeByYearAndSubtype(long yId, eTranSubType sub) {
        return tDao.getTotalAmountFromSubTypeAndYearID(sub, yId);
    };

    public void clearAll(){
        tDao.deleteAll();
        dDao.deleteAll();
        mDao.deleteAll();
        yDao.deleteAll();
    }

    // Update month and year with reverse transaction
    // and next delete it
    public void deleteTransaction(TTransaction transaction){

        TMonth month = mDao.getMonthByID(transaction.getMonthId());
        TYear year = yDao.getYearByID(transaction.getYearId());

        tDao.delete(transaction);
        updateMonth(year, month, transaction, true);

    }

    // Return id of year record
    private TYear findOrCreateYear(int year){
        
        TYear yeardm = yDao.getYearDMByYear(year);
        if(yeardm==null){
            yeardm = new TYear();
            yeardm.setYear(year);
            yeardm.setId(yDao.insert(yeardm));
        }
        
        return yeardm;
    }
    
    // Return id of month record
    private TMonth findOrCreateMonth(int month, long yId){
        
        TMonth monthdm = mDao.getMonthDMByMonthAndYearID(month, yId);
        if(monthdm==null){
           monthdm = new TMonth();
           monthdm.setMonth(month);
           monthdm.setYearId(yId);
           monthdm.setId(mDao.insert(monthdm));
        }
        
        return monthdm;
    }
    
    // Return id of day record
    private TDay findOrCreateDay(int day, long mId, long yId){
        
        TDay daydm = dDao.getDayFromDayAndMonthID(day, mId);
        if(daydm==null){
            daydm = new TDay();
            daydm.setDay(day);
            daydm.setMonthId(mId);
            daydm.setYearId(yId);
            daydm.setId(dDao.insert(daydm));
        }
        
        return daydm;
    }

    private void updateMonth(TYear year, TMonth month, TTransaction trns){
        updateMonth(year, month, trns, false);
    }

    // Update month after transaction. 
    // At the end update also year
    private void updateMonth(TYear year, TMonth month, TTransaction trns, boolean isDel){
        
        double amnt = trns.getAmount();

        if(isDel){
            amnt = (-1)*amnt;
        }

        switch(trns.getTranSubType()){
            case NECESSARY: month.incrementTotNecExp(amnt); break;
            case UNNECESSARY: month.incrementTotUnnExp(amnt); break;
            case EXTRA: month.incrementTotExtraExp(amnt); break;
            case OTHER_INCOME: month.incrementTotOthIncome(amnt); break;
            case SALARY: month.incrementTotIncome(amnt); break;
            case TRADE: month.incrementTrade(amnt); break;
        }
        
        mDao.update(month);
        
        updateYear(year);
        
    }
    
    // Update year after month changes
    private void updateYear(TYear year){
        
        long yearID = year.getId();
        
        int monthNum = mDao.getMonthsFromYearID(yearID).size();
        double annInc = mDao.getTotalYearIncome(yearID);
        double annBal = mDao.getTotalYearBalance(yearID);
        double annExp = mDao.getTotalYearExpenses(yearID);
        double annNec = mDao.getTotalYearNecExp(yearID);
        double annUnn = mDao.getTotalYearUnnExp(yearID);
        double annExtra = mDao.getTotalYearExtraExp(yearID);
        double annTrade = mDao.getTotalTrades(yearID);
        
        year.setMeanIncome(annInc/monthNum);
        year.setIncomeProjection(annInc/monthNum*12);
        year.setMeanBalance(annBal/monthNum);
        year.setBalanceProjection(annBal/monthNum*12);
        year.setMeanExpenses(annExp/monthNum);
        year.setMeanNecExpenses(annNec/monthNum);
        year.setMeanUnnecExpenses(annUnn/monthNum);
        year.setMeanExtraExpenses(annExtra/monthNum);
        year.setNecExpenses(annNec);
        year.setUnnecExpenses(annUnn);
        year.setExtraExpenses(annExtra);
        year.setExpenses(annExp);
        year.setYearTrade(annTrade);
        
        yDao.update(year);
    }
    
    private double[] getPeriodExpensesAnnualMean(int year, int start, int end){
        
        double nec = tDao.getFirstPeriodAnnualMeanExpenses(
                yDao.getYearIDByYear(year), eTranSubType.NECESSARY, start, end);
        double unn = tDao.getFirstPeriodAnnualMeanExpenses(
                yDao.getYearIDByYear(year), eTranSubType.UNNECESSARY, start, end);
        double ext = tDao.getFirstPeriodAnnualMeanExpenses(
                yDao.getYearIDByYear(year), eTranSubType.EXTRA, start, end);
        double tot = nec+unn+ext;
        
        double[] res = {nec/tot, unn/tot, ext/tot};
        return res;
    }
}
