package com.example.provajava.querydao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.provajava.datamodel.TYear;

import java.util.List;

@Dao
public interface TYearDAO {
    
    @Insert
    long insert(TYear year);

    @Insert
    void insert(List<TYear> years);
    
    @Update
    void update(TYear year);
    
    @Query("SELECT ID FROM T_Year WHERE Year = :y")
    long getYearIDByYear(int y);

    @Query("SELECT * FROM T_Year ORDER BY Year")
    List<TYear> getAllYears();

    @Query("SELECT * FROM T_Year WHERE Year = :y")
    TYear getYearDMByYear(int y);
    
    @Query("SELECT * FROM T_Year WHERE ID = :yId")
    TYear getYearByID(long yId);
    
    @Query("UPDATE T_Year SET YearEndTradePosition = :amnt WHERE ID = :yid")
    void updateEndTradePos(double amnt, long yid);

    @Query("DELETE FROM T_Year")
    void deleteAll();
}
