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

package org.gearvrf.videoplayer.manager.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.VideoPlayerApp;

public enum PermissionManager {

    INSTANCE;

    public static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 4000;

    private void showRequestPermissionDialog(@NonNull final Activity activity, @NonNull final OnCheckAllPermissionsListener listener) {

        Context context = VideoPlayerApp.getInstance().getApplicationContext();

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        listener.onPositiveAction();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        listener.onNegativeAction();
                        break;
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false)
                .setMessage(R.string.need_permission_media)
                .setPositiveButton(context.getString(R.string.common_text_settings), dialogClickListener)
                .setNegativeButton(context.getString(R.string.common_text_cancel), dialogClickListener).show();
    }

    public boolean isAllPermissionsGranted() {
        return isExternalStoragePermissionGranted();
    }

    public int getPermission(String permission) {
        Context context = VideoPlayerApp.getInstance().getApplicationContext();
        return ActivityCompat.checkSelfPermission(context, permission);
    }

    public boolean isExternalStoragePermissionGranted() {
        return getPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void checkAllPermissions(@NonNull Activity activity, OnCheckAllPermissionsListener listener) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRequestPermissionDialog(activity, listener);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_ALL_PERMISSIONS);
        }
    }
}
