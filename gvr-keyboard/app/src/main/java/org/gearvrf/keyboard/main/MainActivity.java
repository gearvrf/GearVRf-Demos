/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.keyboard.main;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.keyboard.util.VRSamplesTouchPadGesturesDetector;
import org.gearvrf.keyboard.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

public class MainActivity extends GVRActivity implements
        VRSamplesTouchPadGesturesDetector.OnTouchPadGestureListener {

    float rawY = 0.0f, dist = 0;

    private static final int BUTTON_INTERVAL = 500;
    private static final int TAP_INTERVAL = 300;
    private long mLatestButton = 0;
    private long mLatestTap = 0;
    Main mMain = null;
    private VRSamplesTouchPadGesturesDetector mDetector = null;
    SpeechRecognizer mSpeechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMain = new Main();

        // https://github.com/Samsung/GearVRf/issues/69
        mMain.setActivity(this);
        // mDetector = new VRTouchPadGestureDetector(this);
        setMain(mMain);

        mDetector = new VRSamplesTouchPadGesturesDetector(this, this);
    }

    @Override
    public void onBackPressed() {
        Log.e(null, "teste");
        if (System.currentTimeMillis() > mLatestButton + BUTTON_INTERVAL) {
            mLatestButton = System.currentTimeMillis();
            mMain.onBackPressed();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mLatestButton = System.currentTimeMillis();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        // t.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        Log.d(MainActivity.class.getSimpleName(), "onSingleTap");
        if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
            mLatestTap = System.currentTimeMillis();
            mMain.onSingleTap(e);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(MainActivity.class.getSimpleName(), "onLongPress");
    }

    @Override
    public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection, float velocityX,
            float velocityY) {
        Log.d(MainActivity.class.getSimpleName(), "onSwipe");

        mMain.spinnerListenerAnimation(swipeDirection, velocityY);
        return false;
    }

    public SpeechRecognizer getSpeechRecognizer() {
        return mSpeechRecognizer;
    }

    public void createRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
    }

    public void startRecognizer(RecognitionListener mRecognitionListener) {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
            Intent recognizerIntent = newSpeechIntent();
            mSpeechRecognizer.startListening(recognizerIntent);
        }
    }

    private Intent newSpeechIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        return intent;
    }

    @Override
    public void onSwiping(MotionEvent e, MotionEvent e2, float velocityX, float velocityY,
            SwipeDirection swipeDirection) {
        Log.d(VRSamplesTouchPadGesturesDetector.DEBUG_TAG, "onSwiping() : Call the stop method");
    }

    @Override
    public void onSwipeOppositeLastDirection() {
        Log.d(VRSamplesTouchPadGesturesDetector.DEBUG_TAG,
                "onSwipeOppositeLastDirection() : Call the stop method");
    }
}
