package com.example.provajava.querydao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import com.example.provajava.datamodel.TDay;

import java.util.List;

@Dao
public interface TDayDAO {

    @Insert
    long insert(TDay day);

    @Insert
    void insert(List<TDay> days);
    
    @Update
    void update(TDay day);
    
    @Query("SELECT * FROM T_Day WHERE Month_ID = :monthId")
    List<TDay> getDaysFromMonthID(long monthId);

    @Query("SELECT * FROM T_Day WHERE Month_ID IN( SELECT ID FROM T_Month WHERE Year_ID = :yearId)")
    List<TDay> getDaysFromYearID(long yearId);
    
    @Query("SELECT ID FROM T_Day WHERE Day = :d AND Month_ID = :mId")
    long getDayIDByDayAndMonthID(int d, long mId);
    
    @Query("SELECT * FROM T_Day WHERE Day = :d AND Month_ID = :mId")
    TDay getDayFromDayAndMonthID(int d, long mId);
    
    @Query("SELECT * FROM T_Day WHERE Day = :d AND Month_ID = (SELECT Month_ID FROM T_Month WHERE Month = :m) AND Year_ID = (SELECT Year_ID FROM T_Year WHERE Year = :y)")
    TDay getDayFromDateValues(int d, int m, int y);

    @Query("DELETE FROM T_Day")
    void deleteAll();
}
