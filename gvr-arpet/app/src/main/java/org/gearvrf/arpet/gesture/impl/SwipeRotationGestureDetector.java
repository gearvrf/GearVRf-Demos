/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.gesture.impl;

import android.view.GestureDetector;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.arpet.gesture.OnGestureListener;
import org.gearvrf.arpet.gesture.RotationGestureDetector;

class SwipeRotationGestureDetector extends RotationGestureDetector {

    private static final float VELOCITY = 1.f;
    private static final float DISTANCE_FACTOR = -0.15f;

    private GVRContext mContext;
    private GestureDetector mGestureDetector;
    private float mAngle;
    private boolean mHasDoubleTouch;

    public SwipeRotationGestureDetector(GVRContext context, OnGestureListener listener) {
        super(listener);
        mContext = context;
        initDetector();
    }

    private void initDetector() {
        mContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGestureDetector = new GestureDetector(mContext.getContext(), new LocalGestureListener());
            }
        });
    }

    private class LocalGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mAngle = VELOCITY * distanceX * DISTANCE_FACTOR;
            mListener.onGesture(SwipeRotationGestureDetector.this);
            return true;
        }
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        if (isEnabled()) {

            // Second touch detected
            if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                mHasDoubleTouch = true;
            }
            // Second touch lost
            else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                mHasDoubleTouch = false;
            }

            if (!mHasDoubleTouch) {
                mGestureDetector.onTouchEvent(event);
            }
        }
    }

    @Override
    public float getValue() {
        return mAngle;
    }
}
