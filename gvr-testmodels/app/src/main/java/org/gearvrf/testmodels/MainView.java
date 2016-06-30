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

package org.gearvrf.testmodels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject.BoundingVolume;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

public class MainView extends GVRMain
{

    @SuppressWarnings("unused")
    private static final String TAG = Log
            .tag(MainActivity.class);

    private GVRScene mMainScene;
    private GVRContext mContext;
    private int mCurrentFileIndex;
    private String[] mFileList;
    private GVRModelSceneObject mCurrentModel;
    private final String sEnvironmentPath = Environment.getExternalStorageDirectory().getPath();

    @Override
    public void onInit(GVRContext gvrContext) {
        mContext = gvrContext;
        mMainScene = gvrContext.getNextMainScene();
        mMainScene.setFrustumCulling(true);
        mCurrentFileIndex = -1;
        mCurrentModel = null;
        mFileList = getModelList(sEnvironmentPath + "/GearVRF");
        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);

        switchModel();
    }

    public GVRModelSceneObject loadModel(String filename)
    {
        GVRModelSceneObject model = (GVRModelSceneObject) mMainScene.getSceneObjectByName(filename);
        if (model == null)
        {
            try
            {
                model = mContext.getAssetLoader().loadModel(filename, GVRResourceVolume.VolumeType.ANDROID_SDCARD, mMainScene);
                model.setName(filename);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Failed to load model: %s", e);
                return null;
            }
            BoundingVolume bv = model.getBoundingVolume();
            model.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z - 2 * bv.radius);
        }
        return model;
    }

    @Override
    public void onStep()
    {
    }

    private String[] getModelList(String directory)
    {
        // Add All the Extensions you want to load
        ArrayList<String> extensions = new ArrayList<String>();
        extensions.add(".fbx");
        extensions.add(".3ds");
        extensions.add(".dae");
        extensions.add(".obj");
        extensions.add(".ma");

        // Reads the List of Files from specified folder having extension specified in extensions.
        // Please place your models by creating GVRModelViewer2 folder in your internal phone memory
        File dir = new File(directory);

        if (dir.exists() && dir.isDirectory())
        {
            FilterModels filter = new FilterModels(extensions);
            File list[] = dir.listFiles(filter);
            String[] names = new String[list.length];
            for (int i = 0; i < list.length; ++i)
            {
                names[i] = "GearVRF/" + list[i].getName();
            }
            return names;
        }
        return null;
    }
    
    void onTap()
    {
        switchModel();
    }
    
    void switchModel()
    {
        if (++mCurrentFileIndex >= mFileList.length)
        {
            mCurrentFileIndex = 0;            
        }
        String filename = mFileList[mCurrentFileIndex];
        GVRModelSceneObject model = loadModel(filename);
        if (model == null)
        {
            return;
        }
        if (mCurrentModel != null)
        {
            mCurrentModel.setEnable(false);
        }
        model.setEnable(true);
        mCurrentModel = model;
        screenShot(filename);
    }

    void screenShot(final String filename)
    {
        GVRScreenshotCallback callback = new GVRScreenshotCallback() 
        {
            @Override
            public void onScreenCaptured(Bitmap bitmap)
            {
                try
                {
                     ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                    int i = filename.lastIndexOf('.');
                    File sdcard = Environment.getExternalStorageDirectory();
                    String fname = sdcard.getAbsolutePath() + "/" + filename.substring(0, i) + "_screen.png";
                    File f = new File(fname);
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    Log.d(TAG, "Saved screenshot of %s", filename);
                }
                catch (Exception e)
                {
                    Log.d(TAG, "Could not save screenshot of %s", filename);
                }
            }   
        };
        mContext.captureScreenCenter(callback);
    }
}

class FilterModels implements FilenameFilter
{
    private ArrayList<String> mExtensions;
    public FilterModels(ArrayList<String> extensions)
    {
        mExtensions = extensions;
    }
    @Override
    public boolean accept(File directory, String fileName)
    {
        for (String tExtension : mExtensions)
        {
            if (fileName.endsWith(tExtension))
            {
                return true;
            }
        }
        return false;
    }
}
