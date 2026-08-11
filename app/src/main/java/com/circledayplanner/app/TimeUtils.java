package com.circledayplanner.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class TimeUtils {
    private TimeUtils(){}
    public static String dateKey(Calendar c){ return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime()); }
    public static String header(Calendar c){ return new SimpleDateFormat("EEEE, d. MMMM", Locale.getDefault()).format(c.getTime()); }
    public static String shortDate(Calendar c){ return new SimpleDateFormat("d. MMM", Locale.getDefault()).format(c.getTime()); }
    public static String time(int m){ return String.format(Locale.getDefault(),"%02d:%02d",Math.floorDiv(m,60),m%60); }
    public static String now(){return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date());}
    public static int nowMinute(){Calendar c=Calendar.getInstance(); return c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE);}
    public static int clampMinute(int m){return Math.max(0,Math.min(1440,m));}
    public static int clampStartMinute(int m){return Math.max(0,Math.min(1439,m));}
}
