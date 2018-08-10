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

package org.gearvrf.arpet.gesture.rotation;

import android.view.MotionEvent;

import org.gearvrf.arpet.gesture.GestureDetector;

public class RotationGestureDetector extends GestureDetector {

    private Line mLine1 = new Line(), mLine2 = new Line();
    private float mAngle;

    public interface OnRotationGestureListener {
        void onRotate(RotationGestureDetector detector);
    }

    private static class Point {

        private static final int INVALID_ID = -1;

        int id = INVALID_ID;
        float x, y; // Position

        void reset() {
            id = INVALID_ID;
        }

        boolean isValid() {
            return id != INVALID_ID;
        }

        void setPosition(MotionEvent event) {
            x = event.getX(event.findPointerIndex(id));
            y = event.getY(event.findPointerIndex(id));
        }
    }

    private class Line {

        Point point1 = new Point(), point2 = new Point();

        float angle(Line other) {

            float angle1 = (float) Math.atan2((point1.y - point2.y), (point1.x - point2.x));
            float angle2 = (float) Math.atan2((other.point1.y - other.point2.y), (other.point1.x - other.point2.x));

            float angle = ((float) Math.toDegrees(angle1 - angle2)) % 360;

            if (angle < -180.f) angle += 360.0f;
            if (angle > 180.f) angle -= 360.0f;

            return angle;
        }

        void setPositions(MotionEvent event) {
            point1.setPosition(event);
            point2.setPosition(event);
        }

        boolean isValid() {
            return point1.isValid() && point2.isValid();
        }
    }

    private RotationGestureDetector.OnRotationGestureListener mListener;

    public float getAngle() {
        return mAngle;
    }

    public RotationGestureDetector(RotationGestureDetector.OnRotationGestureListener listener) {
        mListener = listener;
    }

    @Override
    public void onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return;
        }

        switch (event.getActionMasked()) {
            // Detect first touch
            case MotionEvent.ACTION_DOWN:
                mLine1.point1.id = event.getPointerId(event.getActionIndex());
                mLine2.point1.id = mLine1.point1.id;
                break;
            // Detect second touch
            case MotionEvent.ACTION_POINTER_DOWN:
                mLine1.point2.id = event.getPointerId(event.getActionIndex());
                mLine2.point2.id = mLine1.point2.id;
                mLine1.setPositions(event);
                break;
            // Detect rotation angle
            case MotionEvent.ACTION_MOVE:
                if (mLine1.isValid()) {
                    mLine2.setPositions(event);
                    mAngle = mLine1.angle(mLine2);
                    if (mListener != null) {
                        mListener.onRotate(this);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                mLine1.point1.reset();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mLine1.point2.reset();
                break;

            case MotionEvent.ACTION_CANCEL:
                mLine1.point1.reset();
                mLine1.point2.reset();
                break;
        }
    }
}
