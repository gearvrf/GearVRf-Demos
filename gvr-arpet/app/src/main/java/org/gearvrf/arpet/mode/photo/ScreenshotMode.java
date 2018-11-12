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
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.context.ActivityResultEvent;
import org.gearvrf.arpet.context.RequestPermissionResultEvent;
import org.gearvrf.arpet.mode.BasePetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.util.EventBusUtils;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenshotMode extends BasePetMode {

    private static final String TAG = ScreenshotMode.class.getSimpleName();
    private static final String APP_PHOTOS_DIR_NAME = "gvr-arpet";

    private static final int REQUEST_STORAGE_PERMISSION = 1000;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private OnBackToHudModeListener mBackToHudModeListener;
    private PhotoViewController mPhotoViewController;
    private File mPhotosDir;

    public ScreenshotMode(PetContext petContext, OnBackToHudModeListener listener) {
        super(petContext, new PhotoViewController(petContext));
        mBackToHudModeListener = listener;
        mPhotoViewController = new PhotoViewController(petContext);
        mPhotoViewController = (PhotoViewController) mModeScene;

        showViewScreenshot();
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
        requestStoragePermission();
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
    }

    private void showViewScreenshot() {
        IPhotoView view = mPhotoViewController.makeView(IPhotoView.class);
        view.setOnCancelClickListener(
                view1 -> mPetContext.getGVRContext().runOnGlThread(
                        () -> mBackToHudModeListener.OnBackToHud()));
        view.show();
    }

    private void initPhotosDir() {
        if (mPhotosDir == null) {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            mPhotosDir = new File(picturesDir, APP_PHOTOS_DIR_NAME);
            if (!mPhotosDir.exists()) {
                if (mPhotosDir.mkdir()) {
                    Log.d(TAG, "Directory created: " + mPhotosDir);
                }
            } else {
                Log.d(TAG, "Using existing directory: " + mPhotosDir);
            }
        }
    }

    private void takePhoto() {
        initPhotosDir();
        try {
            takePhoto3D();
            //takePhotoCenter();
        } catch (Throwable t) {
            Log.e(TAG, "Error taking photo", t);
        }
    }

    private void takePhotoCenter() {
        Log.d(TAG, "Tacking photo center");
        mPetContext.getGVRContext().captureScreenCenter(this::onPhotoCaptured);
    }

    private void takePhoto3D() {
        Log.d(TAG, "Tacking photo 3D");
        mPetContext.getGVRContext().captureScreen3D((Bitmap[] capturedPhotos) -> {
            if (capturedPhotos != null && capturedPhotos.length > 0) {
                onPhotoCaptured(capturedPhotos[capturedPhotos.length - 1]);
            }
        });
    }

    private void onPhotoCaptured(Bitmap capturedPhoto) {
        Log.d(TAG, "Photo captured " + capturedPhoto);
        if (capturedPhoto != null) {
            showPhoto(capturedPhoto);
            //savePhoto(capturedPhoto); // Optional: save file
        }
    }

    private void showPhoto(Bitmap capturedPhoto) {
        if (IPhotoView.class.isInstance(mPhotoViewController.getCurrentView())) {
            IPhotoView view = (IPhotoView) mPhotoViewController.getCurrentView();
            view.setPhotoBitmap(capturedPhoto);
        }
    }

    private void savePhoto(Bitmap capturedPhoto) {

        final String fileName = "arpet-photo-" + System.currentTimeMillis() + ".png";
        File file = new File(mPhotosDir, fileName);

        try (FileOutputStream output = new FileOutputStream(file)) {
            capturedPhoto.compress(Bitmap.CompressFormat.PNG, 100, output);
            Log.d(TAG, "Photo saved if file: " + file);
        } catch (IOException e) {
            Log.e(TAG, "Error saving photo: " + file, e);
        }
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

    private void requestStoragePermission() {
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
                takePhoto();
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
                takePhoto();
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
}
