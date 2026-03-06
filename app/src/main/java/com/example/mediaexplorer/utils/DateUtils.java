package com.example.mediaexplorer.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String[] last30DaysRange() {
        Calendar end = Calendar.getInstance();
        String dateLte = fmt.format(end.getTime());

        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, -30);
        String dateGte = fmt.format(start.getTime());

        return new String[]{dateGte, dateLte};
    }

    // Сегодня
    public static String today() {
        return fmt.format(Calendar.getInstance().getTime());
    }

    // Диапазон "В кино" = -30 дней до сегодня + 7 дней вперёд
    public static String[] nowPlayingRange() {
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, -30);

        Calendar end = Calendar.getInstance();
        end.add(Calendar.DAY_OF_YEAR, 7);

        return new String[]{
                fmt.format(start.getTime()),
                fmt.format(end.getTime())
        };
    }

    // Диапазон "Скоро" = сегодня + 90 дней
    public static String[] upcoming90DaysRange() {
        Calendar start = Calendar.getInstance();

        Calendar end = Calendar.getInstance();
        end.add(Calendar.DAY_OF_YEAR, 90);

        return new String[]{
                fmt.format(start.getTime()),
                fmt.format(end.getTime())
        };
    }
}