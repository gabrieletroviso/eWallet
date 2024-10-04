package com.example.provajava.dbmanager;

import androidx.room.TypeConverter;
import com.example.provajava.enumerator.*;

public class Converters {

    @TypeConverter
    public static eTranMainType fromTranMainTypeString(String value) {
        return value == null ? null : eTranMainType.valueOf(value);
    }

    @TypeConverter
    public static String TranMainTypeToString(eTranMainType type) {
        return type == null ? null : type.name();
    }
    
    @TypeConverter
    public static eTranSubType fromTranSubTypeString(String value) {
        return value == null ? null : eTranSubType.valueOf(value);
    }

    @TypeConverter
    public static String TranSubTypeToString(eTranMainType type) {
        return type == null ? null : type.name();
    }       
}
