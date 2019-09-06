package com.sdy.jitangapplication.player;

import android.os.CountDownTimer;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * author : ZFM
 * date   : 2019/7/1120:46
 * desc   :
 * version: 1.0
 */
public class UpdateVoiceTimeThread {
    private static CountDownTimer cdt;
    private final static int TIME_CHANGE_DELAY = 1000;
    private final static SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private static String time;
    private static TextView tvv;
    private static UpdateVoiceTimeThread instance = null;
    public static long l;

    public static UpdateVoiceTimeThread getInstance(final String stime, final TextView tv) {
        if (instance == null) {
            synchronized (UpdateVoiceTimeThread.class) {
                if (instance == null) {
                    instance = new UpdateVoiceTimeThread();
                    time = stime;
                    tvv = tv;
                    String smin = stime.substring(0, stime.indexOf(":"));
                    String ssec = stime.substring(stime.indexOf(":") + 1, stime.length());
                    int min = Integer.parseInt(smin);
                    int sec = Integer.parseInt(ssec);
                    l = (min * 60 + sec) * 1000;
                    getTimer();
//                    cdt.start();
                }
            }
        }
        return instance;
    }

    private UpdateVoiceTimeThread() {
    }

    ;

    private static CountDownTimer getTimer() {
        if (cdt != null) {
            cdt.cancel();
            cdt = null;
        }
        cdt = new CountDownTimer(l, TIME_CHANGE_DELAY) {

            @Override
            public void onTick(long millisUntilFinished) {
                tvv.setText(sdf.format(millisUntilFinished));
                l = l - TIME_CHANGE_DELAY;
            }

            @Override
            public void onFinish() {

                tvv.setText(time);

            }

        };
        return cdt;
    }

    public void start() {
        getTimer();
        cdt.start();
    }

    public void pause() {
        cdt.cancel();
        tvv.setText(sdf.format(l));
    }

    public void stop() {
        instance = null;
        if (cdt != null) {
            cdt.cancel();
            cdt = null;
        }
        tvv.setText(time);
    }

}
