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

package org.gearvrf.arpet.manager.permission;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PermissionManager {
    private static final String TAG = PermissionManager.class.getSimpleName();

    @IntDef({PermissionType.CAMERA, PermissionType.LOCATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionType {
        int LOCATION = 0;
        int CAMERA = 1;
    }

    private static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int PERMISSIONS_CODE = 0;

    private Activity mActivity;
    private OnPermissionResultListener mPermissionResultListener;

    public PermissionManager(Activity activity) {
        mActivity = activity;
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(mActivity, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(mActivity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermissions() {
        return hasCameraPermission() && hasLocationPermission();
    }

    public boolean shouldShowRequestPermission(int type) {
        switch (type) {
            case PermissionType.CAMERA:
                return ActivityCompat.shouldShowRequestPermissionRationale(mActivity, CAMERA_PERMISSION);
            case PermissionType.LOCATION:
                return ActivityCompat.shouldShowRequestPermissionRationale(mActivity, LOCATION_PERMISSION);
            default:
                return false;
        }
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(mActivity, new String[] {
                CAMERA_PERMISSION, LOCATION_PERMISSION}, PERMISSIONS_CODE);
    }

    public void handlePermissionResults(int requestCode) {
        if (requestCode == PERMISSIONS_CODE) {
            if (hasPermissions()) {
                mPermissionResultListener.onPermissionGranted();
            } else {
                mPermissionResultListener.onPermissionDenied();
            }
        } else {
            Log.d(TAG, "invalid permission request code");
        }
    }

    public void setPermissionResultListener(OnPermissionResultListener listener) {
        mPermissionResultListener = listener;
    }
}
