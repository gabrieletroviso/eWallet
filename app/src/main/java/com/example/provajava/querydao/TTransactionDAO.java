package com.example.provajava.querydao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import com.example.provajava.datamodel.TTransaction;
import com.example.provajava.enumerator.*;
import java.util.List;

@Dao
public interface TTransactionDAO {
    
    @Insert
    long insert(TTransaction transaction);
    @Insert
    void insert(List<TTransaction> transactions);

    @Delete
    void delete(TTransaction transaction);

    @Update
    void update(TTransaction tran);
    
    @Query("SELECT * FROM T_Transaction WHERE ID = :id")
    TTransaction getTransactionByID(long id);
    
    @Query("SELECT * FROM T_Transaction WHERE Date BETWEEN :startDate AND :endDate")
    List<TTransaction> getTransactionsBetweenDates(long startDate, long endDate);
    
    @Query("SELECT * FROM T_Transaction WHERE Day_ID = :dayId")
    List<TTransaction> getTransactionsFromDayID(long dayId);
    
    @Query("SELECT * FROM T_Transaction WHERE Month_ID = :monthId ORDER BY TransactionMainType")
    List<TTransaction> getTransactionsByMonthIDOrderedByType(long monthId);

    @Query("SELECT * FROM T_Transaction WHERE Month_ID = :monthId ORDER BY TransactionMainType, TransactionSubType")
    List<TTransaction> getTransactionsByMonthIDOrderedBySubType(long monthId);

    @Query("SELECT * FROM T_Transaction WHERE Month_ID = :monthId ORDER BY Date ASC")
    List<TTransaction> getTransactionsByMonthIDOrderedByDate(long monthId);

    @Query("SELECT * FROM T_Transaction WHERE Month_ID = :monthId ORDER BY Amount ASC")
    List<TTransaction> getTransactionsByMonthIDOrderedByAmount(long monthId);
    
    @Query("SELECT * FROM T_Transaction WHERE Year_ID = :yearId")
    List<TTransaction> getTransactionsFromYearID(long yearId);
    
    @Query("SELECT * FROM T_Transaction WHERE TransactionMainType = :type")
    List<TTransaction> getTransactionsByMainType(eTranMainType type);
    
    @Query("SELECT * FROM T_Transaction WHERE TransactionSubType = :type")
    List<TTransaction> getTransactionsBySubType(eTranSubType type);
    
    @Query("SELECT SUM(Amount) FROM T_Transaction WHERE TransactionSubType = :type AND Month_ID = :mId")
    double getTotalAmountFromSubTypeAndMonthID(eTranSubType type, long mId);
    
    @Query("SELECT SUM(Amount) FROM T_Transaction WHERE Date BETWEEN :s AND :e AND TransactionSubType = :sub")
    double getExpensesBetween(long s, long e, eTranSubType sub);
    
    @Query("SELECT SUM(Amount) FROM T_Transaction JOIN T_Day WHERE T_Transaction.Year_ID = :yId AND TransactionSubType = :sub AND Day BETWEEN :s AND :e")
    double getFirstPeriodAnnualMeanExpenses(long yId, eTranSubType sub, int s, int e);
    
    @Query("SELECT * FROM T_Transaction WHERE TransactionMainType = :type AND Amount >= :amount")
    List<TTransaction> getIncomesGreaterThan(eTranMainType type, double amount);
    
    @Query("SELECT * FROM T_Transaction WHERE TransactionMainType = :type AND Amount < :amount")
    List<TTransaction> getIncomesLessThan(eTranMainType type, double amount);
    
    @Query("SELECT SUM(Amount) FROM T_Transaction WHERE TransactionMainType = :type AND Month_ID = :mId")
    double getTotalAmountFromMainTypeAndMonthID(eTranMainType type, long mId);

    @Query("SELECT SUM(Amount) FROM T_Transaction WHERE TransactionSubType = :type AND Year_ID = :yId")
    double getTotalAmountFromSubTypeAndYearID(eTranSubType type, long yId);

    @Query("DELETE FROM T_Transaction")
    void deleteAll();
}
