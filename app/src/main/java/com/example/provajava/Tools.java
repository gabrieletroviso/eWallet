package com.example.provajava;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Tools {

    public static final String APP_NAME = "eWallet";
    private static final String DEFAULT_FORMAT = "dd/MM/yy";
    public static final String DATABASE_NAME = "eWalletDB.db";

    // Italian currency format used by the redesign: "1.318,24 €", "−306,87 €"
    public static String formatEuro(double d){
        return formatEuro(d, false);
    }

    // Same as formatEuro, with an explicit "+" for positive values when signed is true
    public static String formatEuro(double d, boolean signed){
        double r = Math.round(d * 100) / 100.0;
        DecimalFormat f = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.ITALY));
        String sign = r < 0 ? "−" : (signed && r > 0 ? "+" : "");
        return sign + f.format(Math.abs(r)) + " €";
    }

    // Round double in order to have two
    // decimals and return formatted string
    public static String roundToPrint(double d){

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DecimalFormat f = new DecimalFormat("##.00", symbols);
        return f.format(d);

    }

    public static String formattedDate(Date date){
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant();
        return formattedDate(instant.atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public static String formattedDate(LocalDate date){
        return formattedDate(date, DEFAULT_FORMAT);
    }

    public static String formattedDate(LocalDate date, String format){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }
}
