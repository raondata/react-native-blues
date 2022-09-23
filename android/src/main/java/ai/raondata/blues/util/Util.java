package ai.raondata.blues.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Util {
    private static Timer _timerHolder;

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static DateFormat dateFormat() {
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
    }

    public static String formatDate(Date date) {
        return dateFormat().format(date);
    }

    public static Date parseDate(String date) throws ParseException {
        return dateFormat().parse(date);
    }

    public static void setTimer(Runnable task, long duration) {
        if (_timerHolder != null) {
            Log.w("Util.setTimer", "a timer already running");
            return;
        }
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
        timer.schedule(timerTask, 0, duration);
        _timerHolder = timer;
    }

    public static void clearTimer() {
        if (_timerHolder != null) {
            _timerHolder.cancel();
            _timerHolder = null;
        } else {
            Log.w("Util.clearTimer", "a timer already running");
        }
    }
}