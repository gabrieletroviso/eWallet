package com.example.provajava;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Tools {

    private static final String DEFAULT_FORMAT = "dd/MM/yy";
    public static final String DATABASE_NAME = "eWalletDB.db";

    // Round double in order to have two
    // decimals and return formatted string
    public static String roundToPrint(double d){

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DecimalFormat f = new DecimalFormat("##.00", symbols);
        return f.format(d);

    }

    public static String formattedDate(LocalDate date){
        return formattedDate(date, DEFAULT_FORMAT);
    }

    public static String formattedDate(LocalDate date, String format){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }
}
