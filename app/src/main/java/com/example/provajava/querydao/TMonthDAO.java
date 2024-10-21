package com.example.provajava.querydao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import com.example.provajava.datamodel.TMonth;
import com.example.provajava.enumerator.eTranSubType;
import com.example.provajava.enumerator.eTranMainType;
import java.util.List;
        
@Dao
public interface TMonthDAO {

    @Insert
    long insert(TMonth month);

    @Insert
    void insert(List<TMonth> months);

    @Update
    void update(TMonth month);

    @Query("SELECT T_Month.* FROM T_Month JOIN T_Year ON T_Month.Year_ID = T_Year.ID ORDER BY T_Year.Year DESC, T_Month.Month DESC;")
    List<TMonth> getAllOrderedMonths();

    @Query("SELECT * FROM T_Month WHERE Year_ID = :yId ORDER BY Month")
    List<TMonth> getMonthsByYearID(long yId);

    @Query("SELECT * FROM T_Month WHERE ID = :mId")
    TMonth getMonthByID(long mId);

    @Query("SELECT * FROM T_Month WHERE Month =:m AND Year_ID = :yId")
    TMonth getMonthDMByMonthAndYearID(int m, long yId);

    @Query("SELECT * FROM T_Month WHERE Month =:m AND Year_ID = (SELECT ID FROM T_Year WHERE Year = :y)")
    TMonth getMonthDMByMonthAndYear(int m, int y);

    @Query("SELECT ID FROM T_Month WHERE Month =:m AND Year_ID = (SELECT ID FROM T_Year WHERE Year = :y)")
    long getMonthIDByMonthAndYear(int m, int y);

    @Query("SELECT * FROM T_Month WHERE Year_ID = :yearId")
    List<TMonth> getMonthsFromYearID(long yearId);

    @Query("SELECT SUM(TotIncome) FROM T_Month WHERE Year_ID = :yId")
    double getTotalYearIncome(long yId);

    @Query("SELECT SUM(Balance) FROM T_Month WHERE Year_ID = :yId")
    double getTotalYearBalance(long yId);

    @Query("SELECT SUM(TotNecExpenses) FROM T_Month WHERE Year_ID = :yId")
    double getTotalYearNecExp(long yId);

    @Query("SELECT SUM(TotUnnecExpenses) FROM T_Month WHERE Year_ID = :yId")
    double getTotalYearUnnExp(long yId);

    @Query("SELECT SUM(TotExtraExpenses) FROM T_Month WHERE Year_ID = :yId")
    double getTotalYearExtraExp(long yId);

    @Query("SELECT SUM(TotExpenses) FROM T_Month WHERE Year_ID = :yId")
    double getTotalYearExpenses(long yId);

    @Query("SELECT SUM(Trade) FROM T_Month WHERE Year_ID = :yId")
    double getTotalTrades(long yId);

    @Query("SELECT SUM(Amount) FROM T_TRANSACTION WHERE Month_ID = :mId AND TransactionSubType = :type")
    double getTotalMonthAmountBySubtype(long mId, eTranSubType type);

    @Query("SELECT SUM(Amount) FROM T_TRANSACTION WHERE Month_ID = :mId AND TransactionMainType = :type")
    double getTotalMontAmountByMainType(long mId, eTranMainType type);

    @Query("SELECT COUNT(1) FROM T_Month WHERE Year_ID = :yId")
    double getMonthsNumberByYearID(long yId);

    @Query("UPDATE T_Month SET Balance = (SELECT SUM(Amount) FROM T_Transaction WHERE Month_ID = :mId) WHERE id = :mId")
    void updateMonthBalance(long mId);

    @Query("UPDATE T_Month SET EndPosition = :amnt WHERE id = :mId")
    void updateEndPosition(double amnt, long mId);

    @Query("DELETE FROM T_Month")
    void deleteAll();
}
