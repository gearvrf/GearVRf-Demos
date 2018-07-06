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

package org.gearvrf.arpet;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.gearvrf.GVRActivity;
import org.gearvrf.utility.Log;

public class SampleActivity extends GVRActivity {
    private static final String TAG = "GVR_ARPET";

    private SampleMain mMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");


        mMain = new SampleMain();

        setMain(mMain, "gvr.xml");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }


    public static final class CameraPermissionHelper {
        private static final int CAMERA_PERMISSION_CODE = 0;
        private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

        /** Check to see we have the necessary permissions for this app. */
        public static boolean hasCameraPermission(Activity activity) {
            return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED;
        }

        /** Check to see we have the necessary permissions for this app, and ask for them if we don't. */
        public static void requestCameraPermission(Activity activity) {
            ActivityCompat.requestPermissions(
                    activity, new String[] {CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE);
        }

        /** Check to see if we need to show the rationale for this permission. */
        public static boolean shouldShowRequestPermissionRationale(Activity activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION);
        }

        /** Launch Application Setting to grant permission. */
        public static void launchPermissionSettings(Activity activity) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
            activity.startActivity(intent);
        }
    }
}
