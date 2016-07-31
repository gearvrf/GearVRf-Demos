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

package org.gearvrf.balloons;

import org.gearvrf.GVRActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;


public class BalloonActivity extends GVRActivity {
    public BalloonMain main;
    public Camera camera;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        createCameraView();
        main = new BalloonMain(this);
        setMain(main, "gvr.xml");
    }

    @Override
    protected void onPause() {
        if (BalloonMain.sMediaPlayer != null)
            BalloonMain.sMediaPlayer.pause();
        super.onPause();
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        createCameraView();
        if (BalloonMain.sMediaPlayer != null)
            BalloonMain.sMediaPlayer.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        main.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    private long prevTime = 0;
    private PreviewCallback previewCallback = new PreviewCallback() {

        @Override
        /**
         * The byte data comes from the android camera in the yuv format. so we
         * need to convert it to rgba format.
         */
        public void onPreviewFrame(byte[] data, Camera camera) {
            long currentTime = System.currentTimeMillis();
            Log.d(TAG,
                    "Preview Frame rate "
                            + Math.round(1000 / (currentTime - prevTime)));
            prevTime = currentTime;
            camera.addCallbackBuffer(previewCallbackBuffer);
        }
    };

    private byte[] previewCallbackBuffer = null;
    private void createCameraView() {

        if (!checkCameraHardware(this)) {
            android.util.Log.d(TAG, "Camera hardware not available.");
            return;
        }

        camera = null;

        try {
            camera = Camera.open();
            if (camera != null) {
                Parameters params = camera.getParameters();
                params.setZoom(0);
                camera.setParameters(params);

                int bufferSize = params.getPreviewSize().height
                        * params.getPreviewSize().width
                        * ImageFormat
                        .getBitsPerPixel(params.getPreviewFormat()) / 8;
                previewCallbackBuffer = new byte[bufferSize];
                camera.addCallbackBuffer(previewCallbackBuffer);
                camera.setPreviewCallbackWithBuffer(previewCallback);
                camera.startPreview();
            }
        } catch (Exception exception) {
            android.util.Log.d(TAG, "Camera not available or is in use");
        }
    }

    Camera getCamera() {
        return camera;
    }
}
