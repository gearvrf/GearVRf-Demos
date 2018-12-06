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
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.gearvrf.GVRActivity;
import org.gearvrf.arpet.manager.permission.OnPermissionResultListener;
import org.gearvrf.arpet.manager.permission.PermissionManager;
import org.gearvrf.utility.Log;

public class PetActivity extends GVRActivity {
    private static final String TAG = "GVR_ARPET";

    private PetMain mMain;
    private PetContext mPetContext;
    private PermissionManager mPermissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        mPermissionManager = new PermissionManager(this);
        mPermissionManager.setPermissionResultListener(new PermissionListener());
    }

    private void startPetMain() {
        mPetContext = new PetContext(this);
        mMain = new PetMain(mPetContext);
        setMain(mMain, "gvr.xml");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mPermissionManager.hasPermissions()) {
            mPermissionManager.requestPermissions();
        } else if (mPetContext == null) {
            startPetMain();
        } else {
            mPetContext.resume();
            mMain.resume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionManager.handlePermissionResults(requestCode);
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
        public void onPermissionGranted() {
            startPetMain();
        }

        @Override
        public void onPermissionDenied() {
            Log.d(TAG, "on permission denied");
            showMessage(getString(R.string.application_permissions));
            // TODO: maybe we need to call settings here to enable permission again
            finish();
        }

        private void showMessage(String text) {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPetContext.notifyActivityResult(requestCode, resultCode, data);
    }
}
