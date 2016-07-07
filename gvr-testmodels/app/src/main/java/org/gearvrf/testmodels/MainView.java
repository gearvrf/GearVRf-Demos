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
import java.io.IOException;
import java.util.ArrayList;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSceneObject.BoundingVolume;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMain;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.utility.Log;
import org.gearvrf.IAssetEvents;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

public class MainView extends GVRMain
{
    class AssetListener implements IAssetEvents
    {
        @Override
        public void onAssetLoaded(GVRContext ctx, GVRSceneObject model, String filename, String errors)
        {
            if (model != null)
            {
                mState = 2;
                model.setEnable(true);
                if (mCurrentModel != null)
                {
                    mMainScene.removeSceneObject(mCurrentModel);
                }
                mMainScene.addSceneObject(model);
                mCurrentModel = (GVRModelSceneObject) model;
                screenShot(filename);
            }
            else
            {
                mState = 0;
            }
        }
        public void onModelError(GVRContext arg0, String arg1, String arg2) { }
        public void onModelLoaded(GVRContext arg0, GVRSceneObject arg1, String arg2) { }
        public void onTextureError(GVRContext arg0, String arg1, String arg2) { }
        public void onTextureLoaded(GVRContext arg0, GVRTexture arg1, String arg2) { }        
    }

    @SuppressWarnings("unused")
    private static final String TAG = Log
            .tag(MainActivity.class);

    private GVRScene mMainScene;
    private GVRContext mContext;
    private int mCurrentFileIndex;
    private ArrayList<String> mFileList = new ArrayList<String>();
    private GVRModelSceneObject mCurrentModel;
    private int mState = 0;
    private AssetListener mAssetListener;

    @Override
    public void onInit(GVRContext gvrContext) {
        mContext = gvrContext;
        mMainScene = gvrContext.getNextMainScene();
        mMainScene.setFrustumCulling(true);
        mCurrentFileIndex = -1;
        mCurrentModel = null;
        getModelList("GearVRF", mFileList);
        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.WHITE);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        mAssetListener = new AssetListener();
    }

    public GVRModelSceneObject loadModel(String filename)
    {
        GVRModelSceneObject model = (GVRModelSceneObject) mMainScene.getSceneObjectByName(filename);
        if (model == null)
        {
            try
            {
               model = mContext.getAssetLoader().loadModel("sd:" + filename, mAssetListener);
               model.setName(filename);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Failed to load model: %s", e);
                mState = 0;
                return null;
            }
            BoundingVolume bv = model.getBoundingVolume();
            model.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z - 2 * bv.radius);
        }
        return model;
    }

    private void getModelList(String directory, ArrayList<String> fileList)
    {
        final String environmentPath = Environment.getExternalStorageDirectory().getPath();

        // Add All the Extensions you want to load
        ArrayList<String> extensions = new ArrayList<String>();
        extensions.add(".fbx");
        extensions.add(".3ds");
        extensions.add(".dae");
        extensions.add(".obj");
        extensions.add(".ma");

        // Reads the List of Files from specified folder having extension specified in extensions.
        // Please place your models by creating GVRModelViewer2 folder in your internal phone memory
        File dir = new File(environmentPath + "/" + directory);

        if (dir.exists() && dir.isDirectory())
        {
            File list[] = dir.listFiles();
            for (File f : list)
            {
                String fileName = f.getName();
                if (f.isDirectory())
                {
                    getModelList(directory + "/" + fileName, fileList);
                    continue;
                }
                for (String ext: extensions)
                {
                    if (fileName.endsWith(ext))
                    {
                        fileList.add(directory + "/" + fileName);
                        break;
                    }
                }
             }
        }
    }
    
    public void onTap() {}
    
    public void onStep()
    {
        if (mState == 0)
        {
            mState = 1;     // 1 = model loading
            switchModel();
        }
    }
    
    void switchModel()
    {
        if (++mCurrentFileIndex >= mFileList.size())
        {
            --mCurrentFileIndex;
            return;          
        }
        String filename = mFileList.get(mCurrentFileIndex);
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
                    File sdcard = Environment.getExternalStorageDirectory();
                    int i = filename.lastIndexOf("/");
                    String basename = filename;
                    if (i > 0)
                    {
                        basename = filename.substring(i + 1);
                    }
                    basename = FileNameUtils.getBaseName(basename);
                    String fname = sdcard.getAbsolutePath() + "/GearVRF/" + basename + "_screen.png";
                    File f = new File(fname);
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    Log.d(TAG, "Saved screenshot of %s", filename);
                   mState = 0;
                }
                catch (Exception e)
                {
                    Log.d(TAG, "Could not save screenshot of %s", filename);
                    mState = 0;

                }
            }   
        };
        mContext.captureScreenCenter(callback);
    }
}