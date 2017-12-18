package org.gearvrf.simplephysics.entity;

import android.os.CountDownTimer;

import org.gearvrf.GVRContext;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by d.alipio@samsung.com on 11/4/16.
 */

public class Countdown {
    private GVRTextViewSceneObject timeObject;
    private static final String FORMAT = "%02d:%02d";
    private boolean mIsFinished;
    private CountDownTimer mCountDownTimer;

    public Countdown(GVRTextViewSceneObject label) {
        timeObject = label;
        mIsFinished = false;
        mCountDownTimer = null;
    }

    public void start(GVRContext context) {
        mIsFinished = false;
        context.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCountDownTimer = new CountDownTimer(120000, 1000) {

                    public void onTick(long millisUntilFinished) {

                        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished));

                        timeObject.setText("" + String.format(Locale.ENGLISH, FORMAT, minutes, seconds));
                    }

                    public void onFinish() {
                        timeObject.setText("timeout!");
                        mIsFinished = true;
                    }

                };
                mCountDownTimer.start();
            }
        });
    }

    public boolean isFinished() {
        return mIsFinished;
    }

    public void stop(GVRContext context) {
        mIsFinished = true;
        context.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCountDownTimer.cancel();
            }
        });
    }
}
