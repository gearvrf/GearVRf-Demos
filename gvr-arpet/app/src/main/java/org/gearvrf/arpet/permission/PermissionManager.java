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

package org.gearvrf.arpet.permission;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PermissionManager {
    private static final String TAG = PermissionManager.class.getName();

    @IntDef({PermissionType.CAMERA, PermissionType.LOCATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionType {
        int LOCATION = 0;
        int CAMERA = 1;
    }

    private static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int LOCATION_PERMISSION_CODE = 0;
    private static final int CAMERA_PERMISSION_CODE = 1;

    private OnPermissionResultListener mPermissionResultListener;

    public PermissionManager() {

    }

    public boolean hasLocationPermission(@NonNull Activity activity) {
        return ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(@NonNull Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[] {
                LOCATION_PERMISSION}, LOCATION_PERMISSION_CODE);
    }

    public boolean hasCameraPermission(@NonNull Activity activity) {
        return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean shouldShowRequestPermission(@NonNull Activity activity, int type) {
        switch (type) {
            case PermissionType.CAMERA:
                return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION);
            case PermissionType.LOCATION:
                return ActivityCompat.shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION);
            default:
                return false;
        }
    }

    public void requestCameraPermission(@NonNull Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[] {
                CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE);
    }

    public boolean isBluetoothEnabled() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void handlePermissionResults(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionResultListener.onPermissionGranted(PermissionType.CAMERA);
                } else {
                    mPermissionResultListener.onPermissionDenied(PermissionType.CAMERA);
                }
                break;
            case LOCATION_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionResultListener.onPermissionGranted(PermissionType.LOCATION);
                } else {
                    mPermissionResultListener.onPermissionDenied(PermissionType.LOCATION);
                }
                break;
            default:
                Log.d(TAG, "unknown request code");
        }
    }

    public void setPermissionResultListener(OnPermissionResultListener listener) {
        mPermissionResultListener = listener;
    }
}
