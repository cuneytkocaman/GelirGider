package com.cuneyt.gelirgider.assistantclass;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonthYear {

    public String currentlyDateTime(String date){

        DateFormat dateFormat = new SimpleDateFormat(date, new Locale("tr")); // "dd.MM.yyyy HH:mm:ss"
        Calendar calendar = Calendar.getInstance();

        String dt = dateFormat.format(calendar.getTime());

        return dt;

    }
}
