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

package org.gearvrf.arpet.gesture;

import android.support.annotation.FloatRange;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;

public class ScaleGestureDetector extends GestureDetector {

    private static final float MIN_FACTOR = 1.f;
    private static final float MAX_FACTOR = 3.f;

    private android.view.ScaleGestureDetector mScaleGestureDetector;
    private OnScaleGestureListener mOnScaleGestureListener;

    private float mFactor = MIN_FACTOR;
    private float mMinFactor = MIN_FACTOR, mMaxFactor = MAX_FACTOR;
    private GVRContext mContext;

    public ScaleGestureDetector(GVRContext context, OnScaleGestureListener listener) {
        mContext = context;
        mOnScaleGestureListener = listener;
        initDetector();
    }

    private void initDetector() {
        mContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScaleGestureDetector = new android.view.ScaleGestureDetector(mContext.getContext(),
                        new android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            @Override
                            public boolean onScale(android.view.ScaleGestureDetector detector) {
                                mFactor *= detector.getScaleFactor();
                                mFactor = Math.max(mMinFactor, Math.min(mFactor, mMaxFactor));
                                mOnScaleGestureListener.onScale(ScaleGestureDetector.this);
                                return true;
                            }
                        });
            }
        });
    }

    public float getFactor() {
        return mFactor;
    }

    public void onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            mScaleGestureDetector.onTouchEvent(event);
        }
    }

    public void setMinFactor(@FloatRange(from = MIN_FACTOR, to = MAX_FACTOR) float factor) {
        this.mMinFactor = Math.max(MIN_FACTOR, Math.min(factor, MAX_FACTOR));
    }

    public void setMaxFactor(@FloatRange(from = MIN_FACTOR, to = MAX_FACTOR) float factor) {
        this.mMaxFactor = Math.max(MIN_FACTOR, Math.min(factor, MAX_FACTOR));
    }

    public interface OnScaleGestureListener {
        void onScale(ScaleGestureDetector detector);
    }
}
