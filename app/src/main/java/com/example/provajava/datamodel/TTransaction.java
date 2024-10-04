package com.example.provajava.datamodel;

import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Entity;

import com.example.provajava.enumerator.*;

@Entity(
    tableName = "T_Transaction",
    foreignKeys = {
            @ForeignKey(
                    entity = TDay.class,
                    parentColumns = "ID",
                    childColumns = "Day_ID",
                    onDelete = ForeignKey.CASCADE
            ),
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
public class TTransaction {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="ID")
    private long id;
    @ColumnInfo(name="Amount")
    private double amount;
    @ColumnInfo(name="TransactionMainType")
    private eTranMainType tranMainType;
    @ColumnInfo(name="TransactionSubType")
    private eTranSubType tranSubType;
    @ColumnInfo(name="Description")
    private String description;
    @ColumnInfo(name="Date")
    private long date;
    @ColumnInfo(name="Day_ID")
    private long dayId;
    @ColumnInfo(name="Month_ID")
    private long monthId;
    @ColumnInfo(name="Year_ID")
    private long yearId;
    
    public TTransaction() {
        
        amount = 0;
        tranMainType = null;
        tranSubType = null;
        description = null;
        date = -1;
        
        dayId = -1;
        monthId = -1;
        yearId = -1;
    }

    public long getId() {
        return id;
    }
    
    public void setId(long id){
        this.id = id;
    }
    
    public long getDate() {
        return date;
    }

    public void setDate(long d) {
        this.date = d;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description!= null && description.length()>30){
            throw new IllegalArgumentException("Description must be under 30 characters");
        }else{
            this.description = description;
        }
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public eTranSubType getTranSubType(){
        return tranSubType;
    }
    
    public void setTranSubType(eTranSubType type){
        
        switch(type){
            case NECESSARY:
            case EXTRA:
            case UNNECESSARY:
                setTranMainType(eTranMainType.EXPENSE);
                break;
            default:
                setTranMainType(eTranMainType.INCOME);
                break;
        }
        
        this.tranSubType = type;
    }
    
    public long getYearId(){
        return yearId;
    }
    
    public void setYearId(long id){
        yearId = id;
    }
    
    public long getMonthId(){
        return monthId;
    }
    
    public void setMonthId(long id){
        monthId = id;
    }
    
    public long getDayId(){
        return dayId;
    }
    
    public void setDayId(long id){
        dayId = id;
    }

    public eTranMainType getTranMainType() {
        return tranMainType;
    }

    public void setTranMainType(eTranMainType tranMainType) {
        this.tranMainType = tranMainType;
    }
}


