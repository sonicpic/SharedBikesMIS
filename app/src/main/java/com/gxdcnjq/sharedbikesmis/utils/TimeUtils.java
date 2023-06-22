package com.gxdcnjq.sharedbikesmis.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    public static String getCurrentTimestampAsString() {
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        String dateString = dateFormat.format(new Date(timestamp));
        return dateString;
    }

    public static String timestampToStringBase(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = dateFormat.format(new Date(timestamp.getTime()));
        return dateString;
    }

    public static String pythonDatetime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        Date now = new Date();
        return dateFormat.format(now);
    }

    public static String timestampToString(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String dateString = dateFormat.format(new Date(timestamp.getTime()));
        return dateString;
    }

    public static String timestampToStringChinese(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM"+"月"+"dd"+"日"+" HH:mm");
        String dateString = dateFormat.format(new Date(timestamp.getTime()));
        return dateString;
    }

    public static String formatTime(long elapsedTime) {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        return time;
    }

    public static String formatDateTime(String dateTimeString) {
        String INPUT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        String OUTPUT_FORMAT = "MM月dd日 HH时mm分ss秒";
        DateFormat inputFormat = new SimpleDateFormat(INPUT_FORMAT, Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat(OUTPUT_FORMAT, Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateTimeString;
    }

    public static String formatDateTime2(String dateTimeString) {
        final String INPUT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        final String OUTPUT_FORMAT = "MM月dd日 HH时mm分";
        DateFormat inputFormat = new SimpleDateFormat(INPUT_FORMAT, Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat(OUTPUT_FORMAT, Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateTimeString;
    }

}
