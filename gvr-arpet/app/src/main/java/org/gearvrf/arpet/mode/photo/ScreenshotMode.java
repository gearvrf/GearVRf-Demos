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

package org.gearvrf.arpet.mode.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.BuildConfig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.context.ActivityResultEvent;
import org.gearvrf.arpet.context.RequestPermissionResultEvent;
import org.gearvrf.arpet.mode.BasePetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.util.EventBusUtils;
import org.gearvrf.arpet.util.StorageUtils;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenshotMode extends BasePetMode {

    private static final String TAG = ScreenshotMode.class.getSimpleName();

    private static final String PACK_NAME_FACEBOOK = "com.facebook.katana";
    private static final String PACK_NAME_TWITTER = "";
    private static final String PACK_NAME_INSTAGRAM = "";
    private static final String PACK_NAME_WHATSAPP = "com.whatsapp";

    private static final String ACTIVITY_SHARE_PICTURE =
            "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias";

    private static final String APP_PHOTOS_DIR_NAME = "gvr-arpet";
    private static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private static final int REQUEST_STORAGE_PERMISSION = 1000;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private OnBackToHudModeListener mBackToHudModeListener;
    private PhotoViewController mPhotoViewController;
    private File mPhotosDir;
    private OnStoragePermissionGranted mPermissionCallback;
    private File mSavedFile;

    public ScreenshotMode(PetContext petContext, OnBackToHudModeListener listener) {
        super(petContext, new PhotoViewController(petContext));
        mBackToHudModeListener = listener;
        mPhotoViewController = (PhotoViewController) mModeScene;
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
        requestStoragePermission(this::takePhoto);
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
    }

    private void showPhotoView(Bitmap photo) {

        IPhotoView view = mPhotoViewController.makeView(IPhotoView.class);

        view.setOnActionsShareClickListener(this::onSocialAppButtonClicked);

        view.setOnCancelClickListener(
                view1 -> mPetContext.getGVRContext()
                        .runOnGlThread(() -> mBackToHudModeListener.OnBackToHud()));

        view.show();
    }

    private void onSocialAppButtonClicked(View clickedButton) {
        if (clickedButton.getId() == R.id.button_facebook) {
            openFacebook();
        } else if (clickedButton.getId() == R.id.button_whatsapp) {
            openWhatsApp();
        }
    }

    private void initPhotosDir() {
        if (mPhotosDir == null) {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            mPhotosDir = new File(picturesDir, APP_PHOTOS_DIR_NAME);
            if (!mPhotosDir.exists()) {
                if (mPhotosDir.mkdirs()) {
                    Log.d(TAG, "Directory created: " + mPhotosDir);
                }
            } else {
                Log.d(TAG, "Using existing directory: " + mPhotosDir);
            }
        }
    }

    private void takePhoto() {
        try {
            showPhotoView(null);
            //mPetContext.getGVRContext().captureMonoscopicScreen(this::onPhotoCaptured);
            //mPetContext.getGVRContext().captureScreenCenter(this::onPhotoCaptured);
        } catch (Throwable t) {
            Log.e(TAG, "Error taking photo", t);
        }
    }

    private void onPhotoCaptured(Bitmap capturedPhoto) {
        Log.d(TAG, "Photo captured " + capturedPhoto);
        if (capturedPhoto != null) {
            savePhoto(capturedPhoto);
            showPhotoView(capturedPhoto);
        }
    }

    private void savePhoto(Bitmap capturedPhoto) {

        if (StorageUtils.getAvailableExternalStorageSize() <= 0) {
            Log.e(TAG, "There is no free space to save the photo on this device.");
            return;
        }

        initPhotosDir();

        final String fileName = "arpet-photo-" + System.currentTimeMillis() + ".png";
        mSavedFile = new File(mPhotosDir, fileName);

        try (FileOutputStream output = new FileOutputStream(mSavedFile)) {
            capturedPhoto.compress(Bitmap.CompressFormat.PNG, 100, output);
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(mPetContext.getActivity(),
                            "Photo saved", Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            Log.e(TAG, "Error saving photo: " + mSavedFile, e);
            mSavedFile = null;
        }
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

    private void requestStoragePermission(OnStoragePermissionGranted callback) {
        mPermissionCallback = callback;
        mPetContext.getActivity().requestPermissions(PERMISSION_STORAGE, REQUEST_STORAGE_PERMISSION);
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(mPetContext.getActivity(), PERMISSION_STORAGE[0])
                == PackageManager.PERMISSION_GRANTED;
    }

    @Subscribe
    public void handleContextEvent(ActivityResultEvent event) {
        if (event.getRequestCode() == REQUEST_STORAGE_PERMISSION) {
            if (hasStoragePermission()) {
                mPermissionCallback.onGranted();
            } else {
                Toast.makeText(mPetContext.getActivity(),
                        "External storage access not allowed",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe
    public void handleContextEvent(RequestPermissionResultEvent event) {
        if (event.getRequestCode() == REQUEST_STORAGE_PERMISSION) {
            if (hasStoragePermission()) {
                mPermissionCallback.onGranted();
            } else {
                Toast.makeText(mPetContext.getActivity(),
                        "External storage access not allowed",
                        Toast.LENGTH_LONG).show();
                openAppPermissionsSettings();
            }
        }
    }

    private void openAppPermissionsSettings() {
        Activity context = mPetContext.getActivity();
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(intent, REQUEST_STORAGE_PERMISSION);
    }

    private void openFacebook() {
        if (mSavedFile != null && checkAppInstalled(PACK_NAME_FACEBOOK)) {
            Intent intent = createIntent();
            intent.setClassName(PACK_NAME_FACEBOOK, ACTIVITY_SHARE_PICTURE);
            mPetContext.getActivity().startActivity(intent);
        }
    }

    private void openWhatsApp() {
        if (mSavedFile != null && checkAppInstalled(PACK_NAME_WHATSAPP)) {
            Intent intent = createIntent();
            intent.setPackage(PACK_NAME_WHATSAPP);
            mPetContext.getActivity().startActivity(intent);
        }
    }

    private Intent createIntent() {
        Activity context = mPetContext.getActivity();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, mSavedFile));
        intent.setType("image/png");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private boolean checkAppInstalled(String packageName) {
        if (isAppInstalled(packageName)) {
            return true;
        } else {
            installApp(packageName);
            return false;
        }
    }

    private void installApp(String appName) {
        Activity context = mPetContext.getActivity();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName));
            intent.setPackage("com.android.vending");
            context.startActivity(intent);
        } catch (Exception exception) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appName)));
        }
    }

    private boolean isAppInstalled(String packageName) {
        Activity context = mPetContext.getActivity();
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @FunctionalInterface
    private interface OnStoragePermissionGranted {
        void onGranted();
    }

}
