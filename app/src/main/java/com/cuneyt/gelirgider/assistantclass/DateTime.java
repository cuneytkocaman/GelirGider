package com.cuneyt.gelirgider.assistantclass;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTime {

    public String currentlyDateTime(String date){

        DateFormat dateFormat = new SimpleDateFormat(date); // "dd.MM.yyyy HH:mm:ss"
        Calendar calendar = Calendar.getInstance();

        String dt = dateFormat.format(calendar.getTime());

        return dt;

    }
}
