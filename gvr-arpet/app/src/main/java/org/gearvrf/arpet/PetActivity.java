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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.gearvrf.GVRActivity;
import org.gearvrf.arpet.permission.OnPermissionResultListener;
import org.gearvrf.arpet.permission.PermissionManager;
import org.gearvrf.arpet.sharing.AppConnectionManager;
import org.gearvrf.utility.Log;

public class PetActivity extends GVRActivity {
    private static final String TAG = "GVR_ARPET";

    private PetMain mMain;
    private PetContext mPetContext;
    private PermissionManager mPermissionManager;
    private AppConnectionManager mConnectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        mPermissionManager = new PermissionManager();
        mPermissionManager.setPermissionResultListener(new PermissionListener());

        if (!mPermissionManager.hasCameraPermission(this)) {
            mPermissionManager.requestCameraPermission(this);
        } else {
            startPetMain();
        }
    }

    private void startPetMain() {
        mPetContext = new PetContext(this);
        mMain = new PetMain(mPetContext);
        setMain(mMain, "gvr.xml");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mPermissionManager.hasCameraPermission(this)) {
            if (mPermissionManager.shouldShowRequestPermission(this, PermissionManager.PermissionType.CAMERA)) {
                // TODO: show a prompt here to inform the user that the app needs camera permission
                // This prompt should be async and after the user sees the explanation, request the permission
                // again
                Log.d(TAG, "onResume: should show a prompt to request camera permission");
            } else {
                // Don't show any prompt and request the permission again
                mPermissionManager.requestCameraPermission(this);
            }
        } else {
            mPetContext.resume();
            mMain.resume();
        }
        Log.d(TAG, "onResume");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionManager.handlePermissionResults(requestCode, permissions, grantResults);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPetContext != null) {
            mPetContext.pause();
        }
        if (mMain != null) {
            mMain.pause();
        }
    }

    private class PermissionListener implements OnPermissionResultListener {
        @Override
        public void onPermissionGranted(int type) {
            if (type == PermissionManager.PermissionType.CAMERA) {
                startPetMain();
            }
        }

        @Override
        public void onPermissionDenied(int type) {
            if (type == PermissionManager.PermissionType.CAMERA) {
                Toast.makeText(getApplicationContext(), "The application needs camera permission", Toast.LENGTH_LONG).show();
                // TODO: maybe we need to call settings here to enable permission again
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mMain.onActivityResult(requestCode, resultCode);
    }
}
