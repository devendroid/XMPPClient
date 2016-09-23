package com.devs.xmppclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ${Deven} on ${4/1/16}.
 */
public class Utils {
    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");

    public static String getCurrentTime() {

        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    public static String getCurrentDate() {

        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

}