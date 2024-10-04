package com.example.provajava.datamodel;

import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Entity;

@Entity(
        tableName = "T_Day",
        foreignKeys = {
                @ForeignKey(
                        entity = TMonth.class,
                        parentColumns = "ID",
                        childColumns = "Month_ID",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = TYear.class,
                        parentColumns = "ID",
                        childColumns = "Year_ID",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class TDay {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="ID")
    private long id;
    @ColumnInfo(name="Day")
    private int day;
    @ColumnInfo(name="Month_ID")
    private long monthId;
    @ColumnInfo(name="Year_ID")
    private long yearId;
    
    public TDay(){
        day = 0;
        monthId = 0;
        yearId = 0;
    }
    
    public long getId(){
        return id;
    }
    
    public void setId(long dId){
        this.id = dId;
    }
    
    public int getDay(){
        return day;
    }
    
    public void setDay(int day){
        if(day>31 || day<1){
            throw new IllegalArgumentException("Day must be between 1 and 31");
        }else{
            this.day = day;
        }
    }
    
    public long getMonthId(){
        return monthId;
    }
    
    public void setMonthId(long id){
        monthId = id;
    }

    public long getYearId(){
        return yearId;
    }

    public void setYearId(long id){
        yearId = id;
    }

}
