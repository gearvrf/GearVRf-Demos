package org.gearvrf.gvrsimlephysics.entity;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.Gravity;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by d.alipio@samsung.com on 11/4/16.
 */

public class Countdown extends GVRSceneObject {
    private GVRTextViewSceneObject timeObject;
    private static final String FORMAT = "%02d:%02d";

    public Countdown(GVRContext gvrContext, GVRMesh mesh) {
        super(gvrContext, mesh);

        getTransform().setPosition(-3f, 3f, 2f);
        addChildObject(createTimerText());
        setCountdown();
    }

    private GVRSceneObject createTimerText() {
        GVRMesh mesh = getGVRContext().createQuad(10f, 10f);
        GVRSceneObject quadTimer = new GVRSceneObject(getGVRContext(), mesh);
        quadTimer.getTransform().setPosition(2.5f, 10f, 3.3f);
        timeObject = new GVRTextViewSceneObject(getGVRContext());
        timeObject.setTextColor(Color.BLACK);
        timeObject.setGravity(Gravity.CENTER);
        timeObject.setTextSize(20f);
        timeObject.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.LOW);
        timeObject.getTransform().setPosition(2.5f, 8f, 3.5f);
        return timeObject;
    }

    private void setCountdown() {

        getGVRContext().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(180000, 1000) {

                    public void onTick(long millisUntilFinished) {

                        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished));

                        timeObject.setText("" + String.format(Locale.ENGLISH, FORMAT, minutes, seconds));
                    }

                    public void onFinish() {
                        timeObject.setText("done!");
                    }

                }.start();

            }
        });
    }
}
