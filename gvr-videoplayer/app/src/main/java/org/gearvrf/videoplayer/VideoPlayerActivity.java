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

package org.gearvrf.videoplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.gearvrf.GVRActivity;
import org.gearvrf.videoplayer.manager.permission.OnCheckAllPermissionsListener;
import org.gearvrf.videoplayer.manager.permission.PermissionManager;

public class VideoPlayerActivity extends GVRActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    private void checkPermissions() {
        PermissionManager.INSTANCE.checkAllPermissions(this, new OnCheckAllPermissionsListener() {
            @Override
            public void onPositiveAction() {
                launchPermissionSettings();
            }

            @Override
            public void onNegativeAction() {
                Toast.makeText(getBaseContext(), "Permission not granted", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionManager.INSTANCE.isAllPermissionsGranted()) {
            showMainGVRF();
        }
    }

    public void launchPermissionSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, PermissionManager.PERMISSIONS_REQUEST_ALL_PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PermissionManager.PERMISSIONS_REQUEST_ALL_PERMISSIONS) {
            if (PermissionManager.INSTANCE.isAllPermissionsGranted()) {
                showMainGVRF();
            } else {
                Toast.makeText(getBaseContext(), "Permission not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showMainGVRF() {
        setMain(new VideoPlayerMain(), "gvr.xml");
    }
}