package com.example.provajava.dbmanager;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.provajava.datamodel.TDay;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.datamodel.TYear;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.enumerator.eOrder;

import java.time.LocalDate;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DatabaseAccess {

    ExecutorService executor;
    private Engine engine;

    public DatabaseAccess(Context ctx){
        engine = new Engine(ctx);
    }

    // Get year dm
    public TYear getYear(int year){

        executor = Executors.newSingleThreadExecutor();
        TYear ret = null;
        try{
            Future<TYear> future = executor.submit(() -> engine.getYear(year));
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    }

    // Get year dm
    public TYear getYearById(long id){

        executor = Executors.newSingleThreadExecutor();
        TYear ret = null;
        try{
            Future<TYear> future = executor.submit(() -> engine.getYearById(id));
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    }

    // Get all years db
    public List<TYear> getAllYears(){
        executor = Executors.newSingleThreadExecutor();
        List<TYear> ret = null;
        try{
            Future<List<TYear>> future = executor.submit(() -> engine.getYears());
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    }

    // Get month dm
    public TMonth getMonth(int m, int y){

        executor = Executors.newSingleThreadExecutor();
        TMonth ret = null;
        try{
            Future<TMonth> future = executor.submit(() -> engine.getMonth(m, y));
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    }

    // Get all transactions by month id, ordered by specific way
    public List<TTransaction> getTransactionsByMonthID(long mId, eOrder order)
    {
        executor = Executors.newSingleThreadExecutor();
        List<TTransaction> ret = null;
        try{
            Future<List<TTransaction>> future;
            switch (order){
                case SUBTYPE:
                    future = executor.submit(() -> engine.getTransactionsByMonthIDOrderedBySubType(mId)); break;
                case TYPE:
                    future = executor.submit(() -> engine.getTransactionsByMonthIDOrderedByType(mId)); break;
                case VALUE:
                    future = executor.submit(() -> engine.getTransactionsByMonthIDOrderedByValue(mId)); break;
                case DATE:
                default:
                    future = executor.submit(() -> engine.getTransactionsByMonthIDOrderedByDate(mId)); break;
            }
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    };

    // Get month dm
    public TMonth getMonthById(long id){

        executor = Executors.newSingleThreadExecutor();
        TMonth ret = null;
        try{
            Future<TMonth> future = executor.submit(() -> engine.getMonthById(id));
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    }

    // Get all months dm
    public List<TMonth> getOrderedMonths(){
        executor = Executors.newSingleThreadExecutor();
        List<TMonth> ret = null;
        try{
            Future<List<TMonth>> future = executor.submit(() -> engine.getOrderedMonths());
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    }

    // Get months from year id
    public List<TMonth> getMonthsByYearId(long id){
        executor = Executors.newSingleThreadExecutor();
        List<TMonth> ret = null;
        try{
            Future<List<TMonth>> future = executor.submit(() -> engine.getMonthsByYearId(id));
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    }


    //Get day dm
    public TDay getDay(int d, int m, int y){

        executor = Executors.newSingleThreadExecutor();
        TDay ret = null;
        try{
            Future<TDay> future = executor.submit(() -> engine.getDay(d, m, y));
            ret = future.get();
        }catch (ExecutionException | InterruptedException ex){}

        return ret;
    }

    // Add transaction record and manage month and year statistics
    @RequiresApi(api = Build.VERSION_CODES.O)
    public TTransaction addTransaction(LocalDate date, double amount, eTranSubType sub){
        return addTransaction(date, amount, sub, null);
    }

    // Add transaction record with comment and manage month and year statistics
    @RequiresApi(api = Build.VERSION_CODES.O)
    public TTransaction addTransaction(LocalDate date, double amount, eTranSubType sub, String desc) {

        executor = Executors.newSingleThreadExecutor();
        TTransaction ret = null;
        try {
            Future<TTransaction> future = executor.submit(() -> engine.addTransaction(date, amount, sub, desc));
            ret = future.get();
        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    public void updateTransaction(TTransaction transaction){

        executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = executor.submit(() -> engine.updateTransaction(transaction));
            future.get(); // Attende la fine del task
        } catch (InterruptedException | ExecutionException e) {}
        
    }

    // Add monthly final position. If month is december,
    // this method will update year end position also
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addMonthlyFinalPos(LocalDate date, double endPos){
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> engine.addMonthlyFinalPos(date, endPos));
    }

    // Return annual expenses statistics
    public HashMap<eTranSubType, Float> getAnnualExpensesStatistics(int year){

        executor = Executors.newSingleThreadExecutor();
        HashMap<eTranSubType, Float> ret = new HashMap<>();
        double[] val = null;
        try{

            Future<double[]> future = executor.submit(()-> engine.getAnnualHorizontalFirstPeriod(year));
            val = future.get();
            ret.put(eTranSubType.NECESSARY, (float)val[0]);
            ret.put(eTranSubType.UNNECESSARY, (float)val[1]);
            ret.put(eTranSubType.EXTRA, (float)val[2]);

        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Return statistics about expenses between day 1 and day 10
    // on all months of the year
    public HashMap<eTranSubType, Float> getAnnualHorizontalFirstPeriod(int year){

        executor = Executors.newSingleThreadExecutor();
        HashMap<eTranSubType, Float> ret = new HashMap<>();
        double[] val = null;
        try{

            Future<double[]> future = executor.submit(()-> engine.getAnnualHorizontalFirstPeriod(year));
            val = future.get();
            ret.put(eTranSubType.NECESSARY, (float)val[0]);
            ret.put(eTranSubType.UNNECESSARY, (float)val[1]);
            ret.put(eTranSubType.EXTRA, (float)val[2]);

        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Return statistics about expenses between day 11 and day 20
    // on all months of the year
    public HashMap<eTranSubType, Float> getAnnualHorizontalSecondPeriod(int year){

        executor = Executors.newSingleThreadExecutor();
        HashMap<eTranSubType, Float> ret = new HashMap<>();
        double[] val = null;
        try{

            Future<double[]> future = executor.submit(()-> engine.getAnnualHorizontalSecondPeriod(year));
            val = future.get();
            ret.put(eTranSubType.NECESSARY, (float)val[0]);
            ret.put(eTranSubType.UNNECESSARY, (float)val[1]);
            ret.put(eTranSubType.EXTRA, (float)val[2]);

        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Return statistics about expenses between day 21 and day 31
    // on all months of the year
    public HashMap<eTranSubType, Float> getAnnualHorizontalThirdPeriod(int year){

        executor = Executors.newSingleThreadExecutor();
        HashMap<eTranSubType, Float> ret = new HashMap<>();
        double[] val = null;
        try{

            Future<double[]> future = executor.submit(()-> engine.getAnnualHorizontalThirdPeriod(year));
            val = future.get();
            ret.put(eTranSubType.NECESSARY, (float)val[0]);
            ret.put(eTranSubType.UNNECESSARY, (float)val[1]);
            ret.put(eTranSubType.EXTRA, (float)val[2]);

        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Get total monthly income
    public double getMonthlyTotIncomes(long monthId){
        executor = Executors.newSingleThreadExecutor();
        double ret = -1;
        try{
            Future<Double> future = executor.submit(()-> engine.getMonthlyTotIncomes(monthId));
            ret = future.get();
        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Get total monthly expenses
    public double getMonthlyTotExpenses(long monthId){
        executor = Executors.newSingleThreadExecutor();
        double ret = -1;
        try{
            Future<Double> future = executor.submit(()-> engine.getMonthlyTotExpenses(monthId));
            ret = future.get();
        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Get incomes greater than input amount
    public List<TTransaction> getIncomesGreaterThan(double amount) {
        executor = Executors.newSingleThreadExecutor();
        List<TTransaction> ret = null;
        try{
            Future<List<TTransaction>> future = executor.submit(()-> engine.getIncomesGreaterThan(amount));
            ret = future.get();
        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Get incomes less than input amount
    public List<TTransaction> getIncomesLessThan(double amount) {
        executor = Executors.newSingleThreadExecutor();
        List<TTransaction> ret = null;
        try{
            Future<List<TTransaction>> future = executor.submit(()-> engine.getIncomesLessThan(amount));
            ret = future.get();
        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Get annual income with salary subtype
    public double getAnnualSalaryIncome(long yId){

        executor = Executors.newSingleThreadExecutor();
        double ret = -1;
        try{
            Future<Double> future = executor.submit(()-> engine.getIncomeByYearAndSubtype(yId, eTranSubType.SALARY));
            ret = future.get();
        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Get annual income with other subtype
    public double getAnnualOtherIncome(long yId){

        executor = Executors.newSingleThreadExecutor();
        double ret = -1;
        try{
            Future<Double> future = executor.submit(()-> engine.getIncomeByYearAndSubtype(yId, eTranSubType.OTHER_INCOME));
            ret = future.get();
        } catch (ExecutionException | InterruptedException ex) {}

        return ret;
    }

    // Clear all db
    public void clearAllDb(){
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> engine.clearAll());
    }

    // Delete input transaction
    public void deleteTransaction(TTransaction t){
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> engine.deleteTransaction(t));
    }

}
